package com.hemopharm.backend.controller;

import com.hemopharm.backend.dto.BloodTestResponse;
import com.hemopharm.backend.exception.BadRequestException;
import com.hemopharm.backend.service.BloodTestService;
import com.hemopharm.backend.service.ReportExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final ReportExtractionService reportExtractionService;

    @GetMapping
    public List<BloodTestResponse> getBloodTests() {
        return bloodTestService.getBloodTests().stream()
                .map(BloodTestResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public BloodTestResponse getBloodTest(@PathVariable Long id) {
        return BloodTestResponse.from(bloodTestService.getBloodTest(id));
    }

    @Operation(summary = "Upload a lab report; creates a blood test with an auto-generated id")
    @PostMapping(path = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BloodTestResponse extractNew(
            @Parameter(description = "Lab report: PDF, JPEG or PNG")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Optional owner id; defaults to the first user")
            @RequestParam(required = false) Long userId) {
        try {
            return BloodTestResponse.from(
                    reportExtractionService.extractNewReport(file.getBytes(), file.getOriginalFilename(), userId));
        } catch (IOException e) {
            throw new BadRequestException("Could not read uploaded file");
        }
    }

    @Operation(summary = "Extract results into an existing blood test")
    @PostMapping(path = "/{id}/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BloodTestResponse extract(
            @Parameter(description = "Blood test id (see GET /api/blood-tests)", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Lab report: PDF, JPEG or PNG")
            @RequestPart("file") MultipartFile file) {
        try {
            reportExtractionService.extractResults(id, file.getBytes());
            return BloodTestResponse.from(bloodTestService.getBloodTest(id));
        } catch (IOException e) {
            throw new BadRequestException("Could not read uploaded file");
        }
    }
}
