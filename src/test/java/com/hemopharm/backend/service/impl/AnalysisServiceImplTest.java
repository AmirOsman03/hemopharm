package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.Analysis;
import com.hemopharm.backend.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    @Test
    void getAnalyses_returnsAllAnalyses() {
        Analysis analysis = new Analysis();
        analysis.setId(1L);
        when(analysisRepository.findAll()).thenReturn(List.of(analysis));

        assertEquals(List.of(analysis), analysisService.getAnalyses());
    }

    @Test
    void getAnalysis_returnsAnalysis() {
        Analysis analysis = new Analysis();
        analysis.setId(1L);
        when(analysisRepository.findById(1L)).thenReturn(Optional.of(analysis));

        assertEquals(analysis, analysisService.getAnalysis(1L));
    }

    @Test
    void getAnalysis_throwsWhenMissing() {
        when(analysisRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> analysisService.getAnalysis(9L));
    }
}
