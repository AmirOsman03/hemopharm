package com.hemopharm.backend.controller;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.service.BloodTestService;
import com.hemopharm.backend.service.PdfExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BloodTestController.class)
class BloodTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BloodTestService bloodTestService;

    @MockitoBean
    private PdfExtractionService pdfExtractionService;

    @Test
    void getBloodTests_returnsTests() throws Exception {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);
        bloodTest.setFileName("test.pdf");
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        when(bloodTestService.getBloodTests()).thenReturn(List.of(bloodTest));

        mockMvc.perform(get("/api/blood-tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("test.pdf"));
    }

    @Test
    void getBloodTest_returnsResults() throws Exception {
        BloodParameter parameter = new BloodParameter();
        parameter.setId(1L);
        parameter.setName("Hemoglobin");

        BloodTestResult result = new BloodTestResult();
        result.setValue(14.2);
        result.setUnit("g/dL");
        result.setParameter(parameter);

        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        bloodTest.setResults(List.of(result));
        when(bloodTestService.getBloodTest(1L)).thenReturn(bloodTest);

        mockMvc.perform(get("/api/blood-tests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].value").value(14.2))
                .andExpect(jsonPath("$.results[0].parameter.name").value("Hemoglobin"))
                .andExpect(jsonPath("$.results[0].bloodTest").doesNotExist());
    }

    @Test
    void getBloodTest_returns404WhenMissing() throws Exception {
        when(bloodTestService.getBloodTest(9L)).thenThrow(new NotFoundException("Blood test 9 not found"));

        mockMvc.perform(get("/api/blood-tests/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void extract_returnsParsedResults() throws Exception {
        BloodParameter parameter = new BloodParameter();
        parameter.setName("Hemoglobin");

        BloodTestResult result = new BloodTestResult();
        result.setValue(14.2);
        result.setUnit("g/dL");
        result.setParameter(parameter);
        when(pdfExtractionService.extractResults(eq(1L), any(InputStream.class)))
                .thenReturn(List.of(result));

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4".getBytes());

        mockMvc.perform(multipart("/api/blood-tests/1/extract").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(14.2))
                .andExpect(jsonPath("$[0].unit").value("g/dL"));
    }
}
