package com.hemopharm.backend.service;

import com.hemopharm.backend.model.Analysis;

import java.util.List;

public interface AnalysisService {

    List<Analysis> getAnalyses();

    Analysis getAnalysis(Long id);
}
