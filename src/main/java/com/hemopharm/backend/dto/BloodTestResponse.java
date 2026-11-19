package com.hemopharm.backend.dto;

import com.hemopharm.backend.model.Analysis;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.enums.BloodTestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** API shape for a blood test; hides the JPA relationships. */
public record BloodTestResponse(
        Long id,
        String fileName,
        String laboratoryName,
        LocalDateTime testDate,
        LocalDateTime uploadDate,
        BloodTestStatus status,
        SummaryStats summaryStats,
        List<Result> results,
        Analysis latestAnalysis) {

    private static final String NORMAL = "NORMAL";
    private static final String ABNORMAL = "ABNORMAL";

    public record SummaryStats(int totalParameters, int normalCount, int abnormalCount) {
    }

    public record Result(
            Long id,
            Long parameterId,
            String parameterName,
            String category,
            Double value,
            String rawValue,
            String unit,
            ReferenceRange referenceRange,
            String flag) {

        static Result from(BloodTestResult result) {
            BloodParameter parameter = result.getParameter();
            return new Result(
                    result.getId(),
                    parameter == null ? null : parameter.getId(),
                    parameter == null ? null : parameter.getName(),
                    parameter == null ? null : parameter.getCategory(),
                    result.getValue(),
                    result.getRawValue(),
                    result.getUnit(),
                    parameter == null ? null : ReferenceRange.from(parameter),
                    BloodTestResponse.flag(result.getValue(), parameter));
        }
    }

    public record ReferenceRange(Double min, Double max, String formatted) {

        static ReferenceRange from(BloodParameter parameter) {
            return new ReferenceRange(parameter.getMinLimit(), parameter.getMaxLimit(),
                    formatted(parameter.getMinLimit(), parameter.getMaxLimit()));
        }

        private static String formatted(Double min, Double max) {
            if (min == null || max == null) {
                return null;
            }
            return format(min) + " - " + format(max);
        }

        /** 6.1 stays "6.1", 127.0 becomes "127". */
        private static String format(Double value) {
            return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        }
    }

    public static BloodTestResponse from(BloodTest test) {
        List<Result> results = test.getResults().stream().map(Result::from).toList();
        int normalCount = (int) results.stream().filter(result -> NORMAL.equals(result.flag())).count();

        return new BloodTestResponse(
                test.getId(),
                test.getFileName(),
                test.getLaboratoryName(),
                test.getTestDate(),
                test.getUploadDate(),
                test.getStatus(),
                new SummaryStats(results.size(), normalCount, results.size() - normalCount),
                results,
                test.getAnalyses().stream()
                        .max(Comparator.comparing(Analysis::getAnalysisDate))
                        .orElse(null));
    }

    private static String flag(Double value, BloodParameter parameter) {
        if (value == null || parameter == null) {
            return NORMAL;
        }
        boolean belowMin = parameter.getMinLimit() != null && value < parameter.getMinLimit();
        boolean aboveMax = parameter.getMaxLimit() != null && value > parameter.getMaxLimit();
        return belowMin || aboveMax ? ABNORMAL : NORMAL;
    }
}
