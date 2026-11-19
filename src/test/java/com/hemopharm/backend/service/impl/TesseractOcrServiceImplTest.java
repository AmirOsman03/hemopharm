package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TesseractOcrServiceImplTest {

    private final TesseractOcrServiceImpl ocrService = new TesseractOcrServiceImpl("", "eng");

    @Test
    void extractText_throwsWhenBytesAreNotAnImage() {
        assertThrows(BadRequestException.class,
                () -> ocrService.extractText("not an image".getBytes(StandardCharsets.UTF_8)));
    }
}
