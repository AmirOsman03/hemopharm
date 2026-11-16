package com.hemopharm.backend.controller;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.ReferenceRange;
import com.hemopharm.backend.service.BloodParameterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BloodParameterController.class)
class BloodParameterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BloodParameterService bloodParameterService;

    @Test
    void getParameters_returnsParametersWithRanges() throws Exception {
        ReferenceRange range = new ReferenceRange();
        range.setMinValue(13.5);
        range.setMaxValue(17.5);
        range.setUnit("g/dL");

        BloodParameter parameter = new BloodParameter();
        parameter.setId(1L);
        parameter.setName("Hemoglobin");
        parameter.setDefaultUnit("g/dL");
        parameter.setReferenceRanges(List.of(range));
        when(bloodParameterService.getParameters()).thenReturn(List.of(parameter));

        mockMvc.perform(get("/api/parameters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hemoglobin"))
                .andExpect(jsonPath("$[0].referenceRanges[0].maxValue").value(17.5))
                .andExpect(jsonPath("$[0].referenceRanges[0].parameter").doesNotExist());
    }

    @Test
    void getParameter_returnsParameter() throws Exception {
        BloodParameter parameter = new BloodParameter();
        parameter.setId(1L);
        parameter.setName("Glucose");
        when(bloodParameterService.getParameter(1L)).thenReturn(parameter);

        mockMvc.perform(get("/api/parameters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Glucose"));
    }

    @Test
    void getParameter_returns404WhenMissing() throws Exception {
        when(bloodParameterService.getParameter(9L))
                .thenThrow(new NotFoundException("Blood parameter 9 not found"));

        mockMvc.perform(get("/api/parameters/9"))
                .andExpect(status().isNotFound());
    }
}
