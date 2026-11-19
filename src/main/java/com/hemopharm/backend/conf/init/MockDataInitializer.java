package com.hemopharm.backend.conf.init;

import com.hemopharm.backend.model.Analysis;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.Recommendation;
import com.hemopharm.backend.model.User;
import com.hemopharm.backend.model.UserProfile;
import com.hemopharm.backend.model.enums.AnalysisStatus;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.model.enums.Gender;
import com.hemopharm.backend.model.enums.RecommendationPriority;
import com.hemopharm.backend.repository.BloodParameterRepository;
import com.hemopharm.backend.repository.BloodTestRepository;
import com.hemopharm.backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MockDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BloodParameterRepository bloodParameterRepository;
    private final BloodTestRepository bloodTestRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.lab.name:}")
    private String laboratoryName;

    /** Names as they appear in the Macedonian reference report (see img_ref/test.JPG). */
    private static final List<SeedParameter> REPORT_PARAMETERS = List.of(
            new SeedParameter("Глукоза", "Glucose from the reference lab report.", "mmol/L"),
            new SeedParameter("Триглицериди", "Triglycerides from the reference lab report.", "mmol/L"),
            new SeedParameter("Вкупен холестерол", "Total cholesterol from the reference lab report.", "mmol/L"),
            new SeedParameter("Уреа", "Urea from the reference lab report.", "mmol/L"),
            new SeedParameter("Креатинин", "Creatinine from the reference lab report.", "µmol/L"),
            new SeedParameter("Серумско железо", "Serum iron from the reference lab report.", "µmol/L"),
            new SeedParameter("ALT", "Alanine aminotransferase.", "U/L"),
            new SeedParameter("AST", "Aspartate aminotransferase.", "U/L"),
            new SeedParameter("pH", "Urine pH.", null));

    private record SeedParameter(String name, String description, String unit) {
    }

    private record ParameterLimit(Double minLimit, Double maxLimit, String category) {
    }

    private record SeedUser(String name, String email, String password, LocalDate dateOfBirth,
                            Gender gender, String phone, String address) {
    }

    /** Dev fixtures; seeded idempotently by email so existing databases get them too. */
    private static final List<SeedUser> MOCK_USERS = List.of(
            new SeedUser("Amar Hodzic", "amar@example.com", "password123",
                    LocalDate.of(1990, 5, 14), Gender.MALE, "+387 61 123 456", "Sarajevo"),
            new SeedUser("Lejla Karic", "lejla@example.com", "password123",
                    LocalDate.of(1985, 11, 2), Gender.FEMALE, "+387 62 654 321", "Mostar"),
            new SeedUser("Emir Basic", "emir@example.com", "password123",
                    LocalDate.of(1992, 8, 21), Gender.MALE, "+387 61 555 010", "Tuzla"),
            new SeedUser("Sara Novak", "sara@example.com", "password123",
                    LocalDate.of(1998, 2, 9), Gender.FEMALE, "+387 62 555 011", "Banja Luka"),
            new SeedUser("Nikola Petrov", "nikola@example.com", "password123",
                    LocalDate.of(1979, 12, 30), Gender.MALE, "+387 63 555 012", "Zenica"));

    @Override
    public void run(String... args) {
        seedReportParameters();
        seedParameterLimits();
        seedMockUsers();
        seedDemoBloodTests();
    }

    /**
     * Single source of truth for limits: config/parameter-limits.json.
     * Edited limits are applied to the existing parameters on startup.
     */
    private void seedParameterLimits() {
        Map<String, ParameterLimit> limits;
        try (InputStream input = new ClassPathResource("config/parameter-limits.json").getInputStream()) {
            limits = objectMapper.readValue(input, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not read config/parameter-limits.json", e);
        }

        limits.forEach((name, limit) ->
                bloodParameterRepository.findByNameIgnoreCase(name).ifPresent(parameter -> {
                    parameter.setMinLimit(limit.minLimit());
                    parameter.setMaxLimit(limit.maxLimit());
                    parameter.setCategory(limit.category());
                    bloodParameterRepository.save(parameter);
                }));
    }

    private void seedMockUsers() {
        for (SeedUser seed : MOCK_USERS) {
            if (userRepository.findByEmailIgnoreCase(seed.email()).isEmpty()) {
                userRepository.save(user(seed));
            }
        }
    }

    private void seedDemoBloodTests() {
        if (bloodTestRepository.count() > 0) {
            return;
        }

        BloodParameter glucose = bloodParameterRepository.findByNameIgnoreCase("Глукоза").orElseThrow();
        BloodParameter triglycerides = bloodParameterRepository.findByNameIgnoreCase("Триглицериди").orElseThrow();
        BloodParameter cholesterol = bloodParameterRepository.findByNameIgnoreCase("Вкупен холестерол").orElseThrow();

        User amar = userRepository.findByEmailIgnoreCase("amar@example.com").orElseThrow();
        User lejla = userRepository.findByEmailIgnoreCase("lejla@example.com").orElseThrow();

        BloodTest amarTest = bloodTest(amar, "amar-blood-test-2026-11.pdf", 3);
        result(amarTest, glucose, 5.83, "mmol/L");
        result(amarTest, triglycerides, 1.21, "mmol/L");
        result(amarTest, cholesterol, 4.89, "mmol/L");

        Analysis amarAnalysis = analysis(amarTest, 3,
                "Mildly elevated fasting glucose. Triglycerides and total cholesterol are within range.");
        recommendation(amarAnalysis, "Reduce added sugar and refined carbohydrates.", "DIET", RecommendationPriority.HIGH);
        recommendation(amarAnalysis, "Repeat fasting glucose in 3 months.", "FOLLOW_UP", RecommendationPriority.MEDIUM);

        BloodTest lejlaTest = bloodTest(lejla, "lejla-blood-test-2026-10.pdf", 30);
        result(lejlaTest, glucose, 4.9, "mmol/L");
        result(lejlaTest, triglycerides, 1.5, "mmol/L");
        result(lejlaTest, cholesterol, 5.6, "mmol/L");

        Analysis lejlaAnalysis = analysis(lejlaTest, 30,
                "Slightly elevated total cholesterol.");
        recommendation(lejlaAnalysis, "Increase iron-rich foods such as spinach and legumes.", "DIET", RecommendationPriority.HIGH);
        recommendation(lejlaAnalysis, "30 minutes of aerobic exercise 4 times per week.", "LIFESTYLE", RecommendationPriority.MEDIUM);

        bloodTestRepository.saveAll(List.of(amarTest, lejlaTest));
    }

    private BloodParameter parameter(String name, String description, String unit) {
        BloodParameter parameter = new BloodParameter();
        parameter.setName(name);
        parameter.setDescription(description);
        parameter.setDefaultUnit(unit);
        return parameter;
    }

    private void seedReportParameters() {
        for (SeedParameter seed : REPORT_PARAMETERS) {
            if (bloodParameterRepository.findByNameIgnoreCase(seed.name()).isEmpty()) {
                bloodParameterRepository.save(parameter(seed.name(), seed.description(), seed.unit()));
            }
        }
    }

    private User user(SeedUser seed) {
        User user = new User();
        user.setName(seed.name());
        user.setEmail(seed.email());
        user.setPassword(seed.password());

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setDateOfBirth(seed.dateOfBirth());
        profile.setGender(seed.gender());
        profile.setPhone(seed.phone());
        profile.setAddress(seed.address());
        user.setProfile(profile);
        return user;
    }

    private BloodTest bloodTest(User user, String fileName, int daysAgo) {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setUser(user);
        bloodTest.setFileName(fileName);
        bloodTest.setLaboratoryName(laboratoryName);
        bloodTest.setTestDate(LocalDateTime.now().minusDays(daysAgo).minusHours(1));
        bloodTest.setUploadDate(LocalDateTime.now().minusDays(daysAgo));
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        return bloodTest;
    }

    private void result(BloodTest bloodTest, BloodParameter parameter, double value, String unit) {
        BloodTestResult result = new BloodTestResult();
        result.setBloodTest(bloodTest);
        result.setParameter(parameter);
        result.setValue(value);
        result.setRawValue(String.valueOf(value));
        result.setUnit(unit);
        bloodTest.getResults().add(result);
    }

    private Analysis analysis(BloodTest bloodTest, int daysAgo, String summary) {
        Analysis analysis = new Analysis();
        analysis.setBloodTest(bloodTest);
        analysis.setSummary(summary);
        analysis.setAnalysisDate(LocalDateTime.now().minusDays(daysAgo).plusHours(1));
        analysis.setStatus(AnalysisStatus.COMPLETED);
        bloodTest.getAnalyses().add(analysis);
        return analysis;
    }

    private void recommendation(Analysis analysis, String description, String category,
                                RecommendationPriority priority) {
        Recommendation recommendation = new Recommendation();
        recommendation.setAnalysis(analysis);
        recommendation.setDescription(description);
        recommendation.setCategory(category);
        recommendation.setPriority(priority);
        analysis.getRecommendations().add(recommendation);
    }
}
