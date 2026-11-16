package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.repository.BloodParameterRepository;
import com.hemopharm.backend.repository.BloodTestRepository;
import com.hemopharm.backend.service.PdfExtractionService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PdfExtractionServiceImpl implements PdfExtractionService {

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

    private final BloodTestRepository bloodTestRepository;
    private final BloodParameterRepository bloodParameterRepository;

    @Override
    @Transactional
    public List<BloodTestResult> extractResults(Long bloodTestId, InputStream pdf) {
        BloodTest bloodTest = bloodTestRepository.findById(bloodTestId)
                .orElseThrow(() -> new NotFoundException("Blood test " + bloodTestId + " not found"));

        List<BloodTestResult> results = parseResults(bloodTest, extractText(pdf));

        bloodTest.getResults().clear();
        bloodTest.getResults().addAll(results);
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        bloodTestRepository.save(bloodTest);
        return results;
    }

    private String extractText(InputStream pdf) {
        try (PDDocument document = Loader.loadPDF(pdf.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read PDF", e);
        }
    }

    private List<BloodTestResult> parseResults(BloodTest bloodTest, String text) {
        List<BloodTestResult> results = new ArrayList<>();
        for (String line : text.split("\\R")) {
            Matcher reference = REFERENCE_LINE.matcher(line);
            if (reference.matches()) {
                addResult(results, bloodTest, reference.group(1), reference.group(3), reference.group(2));
                continue;
            }

            Matcher referenceNoUnit = REFERENCE_LINE_NO_UNIT.matcher(line);
            if (referenceNoUnit.matches()) {
                addResult(results, bloodTest, referenceNoUnit.group(1), referenceNoUnit.group(2), null);
                continue;
            }

            Matcher simple = SIMPLE_LINE.matcher(line);
            if (simple.matches()) {
                addResult(results, bloodTest, simple.group(1), simple.group(2), simple.group(3));
            }
        }
        return results;
    }

    private void addResult(List<BloodTestResult> results, BloodTest bloodTest,
                           String name, String value, String unit) {
        Optional<BloodParameter> parameter = bloodParameterRepository.findByNameIgnoreCase(name.trim());
        if (parameter.isEmpty()) {
            return;
        }

        BloodTestResult result = new BloodTestResult();
        result.setBloodTest(bloodTest);
        result.setParameter(parameter.get());
        result.setValue(Double.parseDouble(value.replace(',', '.')));
        result.setUnit(unit != null ? unit.trim() : parameter.get().getDefaultUnit());
        results.add(result);
    }
}
