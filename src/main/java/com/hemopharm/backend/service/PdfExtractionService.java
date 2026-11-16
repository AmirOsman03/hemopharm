package com.hemopharm.backend.service;

import com.hemopharm.backend.model.BloodTestResult;

import java.io.InputStream;
import java.util.List;

public interface PdfExtractionService {

    List<BloodTestResult> extractResults(Long bloodTestId, InputStream pdf);
}
