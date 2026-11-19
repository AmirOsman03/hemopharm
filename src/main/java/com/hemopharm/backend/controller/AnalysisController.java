package com.hemopharm.backend.controller;

import com.hemopharm.backend.model.Analysis;
import com.hemopharm.backend.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping
    public List<Analysis> getAnalyses() {
        return analysisService.getAnalyses();
    }

    @GetMapping("/{id}")
    public Analysis getAnalysis(@PathVariable Long id) {
        return analysisService.getAnalysis(id);
    }
}
