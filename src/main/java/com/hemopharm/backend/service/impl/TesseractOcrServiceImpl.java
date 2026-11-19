package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.BadRequestException;
import com.hemopharm.backend.service.OcrService;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class TesseractOcrServiceImpl implements OcrService {

    private static final List<String> COMMON_DATAPATHS = List.of(
            "/opt/homebrew/share/tessdata",
            "/usr/local/share/tessdata",
            "/usr/share/tesseract-ocr/5/tessdata",
            "/usr/share/tesseract-ocr/4.00/tessdata",
            "/usr/share/tessdata");

    private final String datapath;
    private final List<String> languages;

    public TesseractOcrServiceImpl(@Value("${app.ocr.datapath:}") String datapath,
                                   @Value("${app.ocr.language:eng}") String languages) {
        this.datapath = resolveDatapath(datapath);
        this.languages = Arrays.stream(languages.split(","))
                .map(String::trim)
                .filter(language -> !language.isEmpty())
                .toList();
    }

    @Override
    public String extractText(byte[] image) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        } catch (IOException e) {
            throw new BadRequestException("Could not read image");
        }
        if (bufferedImage == null) {
            throw new BadRequestException("Unsupported image format; upload a JPEG or PNG");
        }

        BufferedImage prepared = upscaleIfSmall(bufferedImage);
        StringBuilder text = new StringBuilder();
        RuntimeException failure = null;
        for (String language : languages) {
            try {
                text.append(ocr(prepared, language)).append('\n');
            } catch (BadRequestException e) {
                failure = e;
            }
        }
        if (text.toString().isBlank() && failure != null) {
            throw failure;
        }
        return text.toString();
    }

    private String ocr(BufferedImage image, String language) {
        Tesseract tesseract = new Tesseract();
        if (datapath != null) {
            tesseract.setDatapath(datapath);
        }
        tesseract.setLanguage(language);
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new BadRequestException("Could not OCR image (" + language + "): " + e.getMessage());
        }
    }

    /** Phone screenshots are usually too small for Tesseract; 3x bicubic helps a lot. */
    private BufferedImage upscaleIfSmall(BufferedImage image) {
        if (image.getWidth() >= 1000) {
            return image;
        }
        int width = image.getWidth() * 3;
        int height = image.getHeight() * 3;
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return scaled;
    }

    /** Use the configured path, else TESSDATA_PREFIX, else a known system location. */
    private String resolveDatapath(String configured) {
        if (!configured.isBlank()) {
            return configured;
        }
        String env = System.getenv("TESSDATA_PREFIX");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return COMMON_DATAPATHS.stream()
                .filter(path -> Files.isDirectory(Path.of(path)))
                .findFirst()
                .orElse(null);
    }
}
