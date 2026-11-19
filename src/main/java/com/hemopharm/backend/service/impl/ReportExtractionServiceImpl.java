package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.BadRequestException;
import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.User;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.repository.BloodParameterRepository;
import com.hemopharm.backend.repository.BloodTestRepository;
import com.hemopharm.backend.repository.UserRepository;
import com.hemopharm.backend.service.OcrService;
import com.hemopharm.backend.service.ReportExtractionService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReportExtractionServiceImpl implements ReportExtractionService {

    /**
     * Lab report rows: "Glukoza (mmol/L) 5.83 3.9-6.1".
     * Groups: name, unit, value.
     */
    private static final Pattern REFERENCE_LINE = Pattern.compile(
            "^\\s*(.+?)\\s*\\(([^)]+)\\)\\s*(\\d+(?:[.,]\\d+)?)\\s+\\d+(?:[.,]\\d+)?\\s*-\\s*\\d+(?:[.,]\\d+)?\\s*$");

    /**
     * Rows without a unit: "pH 5.5 4.8-7.2".
     * Groups: name, value.
     */
    private static final Pattern REFERENCE_LINE_NO_UNIT = Pattern.compile(
            "^\\s*(.+?)\\s+(\\d+(?:[.,]\\d+)?)\\s+\\d+(?:[.,]\\d+)?\\s*-\\s*\\d+(?:[.,]\\d+)?\\s*$");

    /**
     * Simple rows: "Hemoglobin 14.2 g/dL" or "Total Cholesterol: 190 mg/dL".
     * Groups: name, value, unit.
     */
    private static final Pattern SIMPLE_LINE = Pattern.compile(
            "^\\s*([A-Za-z][A-Za-z0-9 /().%-]*?)\\s*[:=]?\\s+(\\d+(?:[.,]\\d+)?)\\s*([A-Za-z/%µ]+)?\\s*$");

    /** Report header: "11/4/2026 7:49:55 AM" (Cyrillic АМ/ПМ is normalized). */
    private static final Pattern TEST_DATE = Pattern.compile(
            "(\\d{1,2})/(\\d{1,2})/(\\d{4})\\s+(\\d{1,2}):(\\d{2}):(\\d{2})\\s*([AaPpАаПп][MmМм])");

    private final BloodTestRepository bloodTestRepository;
    private final BloodParameterRepository bloodParameterRepository;
    private final UserRepository userRepository;
    private final OcrService ocrService;

    @Value("${app.lab.name:}")
    private String laboratoryName;

    @Override
    @Transactional
    public List<BloodTestResult> extractResults(Long bloodTestId, byte[] content) {
        BloodTest bloodTest = bloodTestRepository.findById(bloodTestId)
                .orElseThrow(() -> new NotFoundException("Blood test " + bloodTestId + " not found"));

        String text = extractText(content);
        List<BloodTestResult> results = parseResults(bloodTest, text);
        if (results.isEmpty()) {
            throw new BadRequestException("No known blood parameters found in the uploaded document");
        }

        bloodTest.getResults().clear();
        bloodTest.getResults().addAll(results);
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        parseTestDate(text).ifPresent(bloodTest::setTestDate);
        bloodTestRepository.save(bloodTest);
        return results;
    }

    @Override
    @Transactional
    public BloodTest extractNewReport(byte[] content, String fileName, Long userId) {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setUser(resolveUser(userId));
        bloodTest.setFileName(fileName == null || fileName.isBlank() ? "report" : fileName);
        bloodTest.setLaboratoryName(laboratoryName == null || laboratoryName.isBlank() ? null : laboratoryName);
        bloodTest.setUploadDate(LocalDateTime.now());

        String text = extractText(content);
        List<BloodTestResult> results = parseResults(bloodTest, text);
        if (results.isEmpty()) {
            throw new BadRequestException("No known blood parameters found in the uploaded document");
        }

        bloodTest.setTestDate(parseTestDate(text).orElse(null));
        bloodTest.getResults().addAll(results);
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        return bloodTestRepository.save(bloodTest);
    }

    private Optional<LocalDateTime> parseTestDate(String text) {
        Matcher matcher = TEST_DATE.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String meridiem = matcher.group(7).toUpperCase()
                .replace('А', 'A')
                .replace('М', 'M')
                .replace('П', 'P');
        int hour = Integer.parseInt(matcher.group(4));
        if ("AM".equals(meridiem) && hour == 12) {
            hour = 0;
        } else if ("PM".equals(meridiem) && hour < 12) {
            hour += 12;
        }

        return Optional.of(LocalDateTime.of(
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(1)),
                hour,
                Integer.parseInt(matcher.group(5)),
                Integer.parseInt(matcher.group(6))));
    }

    private User resolveUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        }
        return userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BadRequestException("No users found; create a user before uploading a report"));
    }

    private String extractText(byte[] content) {
        String text;
        if (isPdf(content)) {
            text = extractPdfText(content);
        } else if (isJpeg(content) || isPng(content)) {
            text = ocrService.extractText(content);
        } else {
            throw new BadRequestException("Unsupported file; upload a PDF, JPEG or PNG");
        }

        if (text.isBlank()) {
            throw new BadRequestException("No text found in the uploaded file");
        }
        return text;
    }

    private String extractPdfText(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new BadRequestException("Could not read PDF: file is corrupted or not a PDF");
        }
    }

    /** A PDF header may appear anywhere in the first 1024 bytes. */
    private boolean isPdf(byte[] bytes) {
        for (int i = 0; i <= Math.min(bytes.length - 5, 1024); i++) {
            if (bytes[i] == '%' && bytes[i + 1] == 'P' && bytes[i + 2] == 'D'
                    && bytes[i + 3] == 'F' && bytes[i + 4] == '-') {
                return true;
            }
        }
        return false;
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
    }

    private List<BloodTestResult> parseResults(BloodTest bloodTest, String text) {
        List<BloodTestResult> results = new ArrayList<>();
        Set<String> seenParameters = new HashSet<>();
        for (String line : text.split("\\R")) {
            Matcher reference = REFERENCE_LINE.matcher(line);
            if (reference.matches()) {
                addResult(results, seenParameters, bloodTest, reference.group(1), reference.group(3), reference.group(2));
                continue;
            }

            Matcher referenceNoUnit = REFERENCE_LINE_NO_UNIT.matcher(line);
            if (referenceNoUnit.matches()) {
                addResult(results, seenParameters, bloodTest, referenceNoUnit.group(1), referenceNoUnit.group(2), null);
                continue;
            }

            Matcher simple = SIMPLE_LINE.matcher(line);
            if (simple.matches()) {
                addResult(results, seenParameters, bloodTest, simple.group(1), simple.group(2), simple.group(3));
            }
        }
        return results;
    }

    private void addResult(List<BloodTestResult> results, Set<String> seenParameters, BloodTest bloodTest,
                           String name, String value, String unit) {
        Optional<BloodParameter> parameter = bloodParameterRepository.findByNameIgnoreCase(name.trim());
        // OCR runs once per language pack, so the same row can be read twice; keep the first hit.
        if (parameter.isEmpty() || !seenParameters.add(parameter.get().getName().toLowerCase())) {
            return;
        }

        BloodTestResult result = new BloodTestResult();
        result.setBloodTest(bloodTest);
        result.setParameter(parameter.get());
        result.setValue(Double.parseDouble(value.replace(',', '.')));
        result.setRawValue(value.trim());
        result.setUnit(isPlausibleUnit(unit) ? unit.trim() : parameter.get().getDefaultUnit());
        results.add(result);
    }

    /** Units contain Latin letters (mmol/L, U/L, g/dL); OCR often mangles the small ones. */
    private boolean isPlausibleUnit(String unit) {
        return unit != null && unit.matches(".*[A-Za-z].*");
    }
}
