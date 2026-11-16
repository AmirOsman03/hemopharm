package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.Analysis;
import com.hemopharm.backend.repository.AnalysisRepository;
import com.hemopharm.backend.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRepository analysisRepository;

    @Override
    public List<Analysis> getAnalyses() {
        return analysisRepository.findAll();
    }

    @Override
    public Analysis getAnalysis(Long id) {
        return analysisRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Analysis " + id + " not found"));
    }
}
