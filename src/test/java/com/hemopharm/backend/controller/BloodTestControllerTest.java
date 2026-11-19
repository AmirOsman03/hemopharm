package com.hemopharm.backend.controller;

import com.hemopharm.backend.exception.BadRequestException;
import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.service.BloodTestService;
import com.hemopharm.backend.service.ReportExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
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
    private ReportExtractionService reportExtractionService;

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
        parameter.setCategory("Hematology");
        parameter.setDescription("Oxygen-carrying protein.");
        parameter.setDefaultUnit("g/dL");
        parameter.setMinLimit(13.5);
        parameter.setMaxLimit(17.5);

        BloodTestResult result = new BloodTestResult();
        result.setId(11L);
        result.setValue(14.2);
        result.setRawValue("14.2");
        result.setUnit("g/dL");
        result.setParameter(parameter);

        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);
        bloodTest.setFileName("test.pdf");
        bloodTest.setLaboratoryName("Test Lab");
        bloodTest.setTestDate(LocalDateTime.of(2026, 4, 11, 7, 49, 55));
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        bloodTest.setResults(List.of(result));
        when(bloodTestService.getBloodTest(1L)).thenReturn(bloodTest);

        mockMvc.perform(get("/api/blood-tests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("test.pdf"))
                .andExpect(jsonPath("$.laboratoryName").value("Test Lab"))
                .andExpect(jsonPath("$.testDate").value("2026-04-11T07:49:55"))
                .andExpect(jsonPath("$.summaryStats.totalParameters").value(1))
                .andExpect(jsonPath("$.summaryStats.normalCount").value(1))
                .andExpect(jsonPath("$.summaryStats.abnormalCount").value(0))
                .andExpect(jsonPath("$.results[0].id").value(11))
                .andExpect(jsonPath("$.results[0].parameterId").value(1))
                .andExpect(jsonPath("$.results[0].parameterName").value("Hemoglobin"))
                .andExpect(jsonPath("$.results[0].category").value("Hematology"))
                .andExpect(jsonPath("$.results[0].value").value(14.2))
                .andExpect(jsonPath("$.results[0].rawValue").value("14.2"))
                .andExpect(jsonPath("$.results[0].unit").value("g/dL"))
                .andExpect(jsonPath("$.results[0].referenceRange.min").value(13.5))
                .andExpect(jsonPath("$.results[0].referenceRange.max").value(17.5))
                .andExpect(jsonPath("$.results[0].referenceRange.formatted").value("13.5 - 17.5"))
                .andExpect(jsonPath("$.results[0].flag").value("NORMAL"))
                .andExpect(jsonPath("$.results[0].parameter").doesNotExist())
                .andExpect(jsonPath("$.results[0].bloodTest").doesNotExist())
                .andExpect(jsonPath("$.analyses").doesNotExist());
    }

    @Test
    void getBloodTest_flagsOutOfRangeResultAsAbnormal() throws Exception {
        BloodParameter parameter = new BloodParameter();
        parameter.setId(2L);
        parameter.setName("Глукоза");
        parameter.setMinLimit(3.9);
        parameter.setMaxLimit(6.1);

        BloodTestResult result = new BloodTestResult();
        result.setId(12L);
        result.setValue(7.2);
        result.setParameter(parameter);

        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(2L);
        bloodTest.setResults(List.of(result));
        when(bloodTestService.getBloodTest(2L)).thenReturn(bloodTest);

        mockMvc.perform(get("/api/blood-tests/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].flag").value("ABNORMAL"))
                .andExpect(jsonPath("$.summaryStats.normalCount").value(0))
                .andExpect(jsonPath("$.summaryStats.abnormalCount").value(1));
    }

    @Test
    void getBloodTest_returns404WhenMissing() throws Exception {
        when(bloodTestService.getBloodTest(9L)).thenThrow(new NotFoundException("Blood test 9 not found"));

        mockMvc.perform(get("/api/blood-tests/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void extractNew_returnsCreatedBloodTestWithGeneratedId() throws Exception {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(42L);
        bloodTest.setFileName("test.jpg");
        when(reportExtractionService.extractNewReport(any(byte[].class), eq("test.jpg"), isNull()))
                .thenReturn(bloodTest);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "jpeg".getBytes());

        mockMvc.perform(multipart("/api/blood-tests/extract").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.fileName").value("test.jpg"));
    }

    @Test
    void extractNew_passesUserIdWhenGiven() throws Exception {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(43L);
        when(reportExtractionService.extractNewReport(any(byte[].class), eq("test.jpg"), eq(3L)))
                .thenReturn(bloodTest);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "jpeg".getBytes());

        mockMvc.perform(multipart("/api/blood-tests/extract").file(file).param("userId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(43));

        verify(reportExtractionService).extractNewReport(any(byte[].class), eq("test.jpg"), eq(3L));
    }

    @Test
    void extract_returnsParsedResults() throws Exception {
        BloodParameter parameter = new BloodParameter();
        parameter.setId(1L);
        parameter.setName("Hemoglobin");
        parameter.setMinLimit(13.5);
        parameter.setMaxLimit(17.5);

        BloodTestResult result = new BloodTestResult();
        result.setId(11L);
        result.setValue(14.2);
        result.setRawValue("14.2");
        result.setUnit("g/dL");
        result.setParameter(parameter);

        BloodTest bloodTest = new BloodTest();
        bloodTest.setId(1L);
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        bloodTest.setResults(List.of(result));

        when(reportExtractionService.extractResults(eq(1L), any(byte[].class)))
                .thenReturn(List.of(result));
        when(bloodTestService.getBloodTest(1L)).thenReturn(bloodTest);

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4".getBytes());

        mockMvc.perform(multipart("/api/blood-tests/1/extract").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.results[0].parameterName").value("Hemoglobin"))
                .andExpect(jsonPath("$.results[0].value").value(14.2))
                .andExpect(jsonPath("$.results[0].rawValue").value("14.2"))
                .andExpect(jsonPath("$.results[0].unit").value("g/dL"))
                .andExpect(jsonPath("$.results[0].referenceRange.formatted").value("13.5 - 17.5"))
                .andExpect(jsonPath("$.results[0].flag").value("NORMAL"));
    }

    @Test
    void extract_returns400WhenFileIsUnsupported() throws Exception {
        when(reportExtractionService.extractResults(eq(1L), any(byte[].class)))
                .thenThrow(new BadRequestException("Unsupported file; upload a PDF, JPEG or PNG"));

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "not a pdf".getBytes());

        mockMvc.perform(multipart("/api/blood-tests/1/extract").file(file))
                .andExpect(status().isBadRequest());
    }
}
