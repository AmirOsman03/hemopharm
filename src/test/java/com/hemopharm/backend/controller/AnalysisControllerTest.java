package com.hemopharm.backend.controller;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.Analysis;
import com.hemopharm.backend.model.Recommendation;
import com.hemopharm.backend.model.enums.AnalysisStatus;
import com.hemopharm.backend.model.enums.RecommendationPriority;
import com.hemopharm.backend.service.AnalysisService;
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

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void getAnalyses_returnsAnalysesWithRecommendations() throws Exception {
        Recommendation recommendation = new Recommendation();
        recommendation.setDescription("Reduce sugar intake.");
        recommendation.setCategory("DIET");
        recommendation.setPriority(RecommendationPriority.HIGH);

        Analysis analysis = new Analysis();
        analysis.setId(1L);
        analysis.setSummary("Mildly elevated glucose.");
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setRecommendations(List.of(recommendation));
        when(analysisService.getAnalyses()).thenReturn(List.of(analysis));

        mockMvc.perform(get("/api/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].summary").value("Mildly elevated glucose."))
                .andExpect(jsonPath("$[0].recommendations[0].category").value("DIET"))
                .andExpect(jsonPath("$[0].recommendations[0].analysis").doesNotExist());
    }

    @Test
    void getAnalysis_returnsAnalysis() throws Exception {
        Analysis analysis = new Analysis();
        analysis.setId(1L);
        analysis.setStatus(AnalysisStatus.COMPLETED);
        when(analysisService.getAnalysis(1L)).thenReturn(analysis);

        mockMvc.perform(get("/api/analyses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getAnalysis_returns404WhenMissing() throws Exception {
        when(analysisService.getAnalysis(9L)).thenThrow(new NotFoundException("Analysis 9 not found"));

        mockMvc.perform(get("/api/analyses/9"))
                .andExpect(status().isNotFound());
    }
}
