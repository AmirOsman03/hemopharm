package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.repository.BloodParameterRepository;
import com.hemopharm.backend.repository.BloodTestRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExtractionServiceImplTest {

    @Mock
    private BloodTestRepository bloodTestRepository;

    @Mock
    private BloodParameterRepository bloodParameterRepository;

    @InjectMocks
    private PdfExtractionServiceImpl pdfExtractionService;

    @Test
    void extractResults_parsesKnownParameters() throws IOException {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);

        BloodParameter hemoglobin = parameter("Hemoglobin", "g/dL");
        BloodParameter glucose = parameter("Fasting Glucose", "mg/dL");

        when(bloodTestRepository.findById(1L)).thenReturn(Optional.of(bloodTest));
        when(bloodParameterRepository.findByNameIgnoreCase("Hemoglobin")).thenReturn(Optional.of(hemoglobin));
        when(bloodParameterRepository.findByNameIgnoreCase("Fasting Glucose")).thenReturn(Optional.of(glucose));

        byte[] pdf = pdfWith(
                "Blood Test Report",
                "Hemoglobin 14.2 g/dL",
                "Fasting Glucose: 105 mg/dL",
                "Potassium 4.1 mmol/L");

        List<BloodTestResult> results = pdfExtractionService.extractResults(1L, new ByteArrayInputStream(pdf));

        assertEquals(2, results.size());
        assertEquals("Hemoglobin", results.get(0).getParameter().getName());
        assertEquals(14.2, results.get(0).getValue(), 0.001);
        assertEquals("g/dL", results.get(0).getUnit());
        assertEquals("Fasting Glucose", results.get(1).getParameter().getName());
        assertEquals(105.0, results.get(1).getValue(), 0.001);
        assertEquals(bloodTest, results.get(0).getBloodTest());
        assertEquals(BloodTestStatus.COMPLETED, bloodTest.getStatus());
        verify(bloodTestRepository).save(bloodTest);
    }

    @Test
    void extractResults_usesDefaultUnitWhenPdfHasNone() throws IOException {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);
        BloodParameter hemoglobin = parameter("Hemoglobin", "g/dL");

        when(bloodTestRepository.findById(1L)).thenReturn(Optional.of(bloodTest));
        when(bloodParameterRepository.findByNameIgnoreCase("Hemoglobin")).thenReturn(Optional.of(hemoglobin));

        List<BloodTestResult> results = pdfExtractionService.extractResults(
                1L, new ByteArrayInputStream(pdfWith("Hemoglobin 14.2")));

        assertEquals(1, results.size());
        assertEquals("g/dL", results.get(0).getUnit());
    }

    @Test
    void extractResults_parsesReferenceReportFormat() throws IOException {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);

        when(bloodTestRepository.findById(1L)).thenReturn(Optional.of(bloodTest));
        when(bloodParameterRepository.findByNameIgnoreCase("Glukoza"))
                .thenReturn(Optional.of(parameter("Glukoza", "mmol/L")));
        when(bloodParameterRepository.findByNameIgnoreCase("Trigliceridi"))
                .thenReturn(Optional.of(parameter("Trigliceridi", "mmol/L")));
        when(bloodParameterRepository.findByNameIgnoreCase("ALT"))
                .thenReturn(Optional.of(parameter("ALT", "U/L")));
        when(bloodParameterRepository.findByNameIgnoreCase("pH"))
                .thenReturn(Optional.of(parameter("pH", null)));

        byte[] pdf = pdfWith(
                "Biohemijska analiza",
                "Glukoza (mmol/L) 5.83 3.9-6.1",
                "Trigliceridi (mmol/L) 1.21 0.68- 1.8",
                "ALT (U/L) 15 9-43",
                "pH 5.5 4.8-7.2",
                "Ketoni Negativan Negativan");

        List<BloodTestResult> results = pdfExtractionService.extractResults(1L, new ByteArrayInputStream(pdf));

        assertEquals(4, results.size());
        assertEquals("Glukoza", results.get(0).getParameter().getName());
        assertEquals(5.83, results.get(0).getValue(), 0.001);
        assertEquals("mmol/L", results.get(0).getUnit());
        assertEquals("Trigliceridi", results.get(1).getParameter().getName());
        assertEquals(1.21, results.get(1).getValue(), 0.001);
        assertEquals("ALT", results.get(2).getParameter().getName());
        assertEquals(15.0, results.get(2).getValue(), 0.001);
        assertEquals("pH", results.get(3).getParameter().getName());
        assertEquals(5.5, results.get(3).getValue(), 0.001);
    }

    @Test
    void extractResults_throwsWhenBloodTestMissing() {
        when(bloodTestRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> pdfExtractionService.extractResults(9L, new ByteArrayInputStream(new byte[0])));
    }

    private BloodParameter parameter(String name, String unit) {
        BloodParameter parameter = new BloodParameter();
        parameter.setName(name);
        parameter.setDefaultUnit(unit);
        return parameter;
    }

    private byte[] pdfWith(String... lines) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                for (String line : lines) {
                    content.showText(line);
                    content.newLineAtOffset(0, -20);
                }
                content.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
