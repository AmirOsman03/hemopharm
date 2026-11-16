package com.hemopharm.backend.controller;

import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.service.BloodTestService;
import com.hemopharm.backend.service.PdfExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/blood-tests")
@RequiredArgsConstructor
public class BloodTestController {

    private final BloodTestService bloodTestService;
    private final PdfExtractionService pdfExtractionService;

    @GetMapping
    public List<BloodTest> getBloodTests() {
        return bloodTestService.getBloodTests();
    }

    @GetMapping("/{id}")
    public BloodTest getBloodTest(@PathVariable Long id) {
        return bloodTestService.getBloodTest(id);
    }

    @PostMapping(path = "/{id}/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<BloodTestResult> extract(@PathVariable Long id,
                                         @RequestPart("file") MultipartFile file) throws IOException {
        return pdfExtractionService.extractResults(id, file.getInputStream());
    }
}
