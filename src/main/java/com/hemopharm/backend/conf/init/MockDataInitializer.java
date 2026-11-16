package com.hemopharm.backend.conf.init;

import com.hemopharm.backend.model.Analysis;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.model.BloodTestResult;
import com.hemopharm.backend.model.Recommendation;
import com.hemopharm.backend.model.ReferenceRange;
import com.hemopharm.backend.model.User;
import com.hemopharm.backend.model.UserProfile;
import com.hemopharm.backend.model.enums.AnalysisStatus;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import com.hemopharm.backend.model.enums.Gender;
import com.hemopharm.backend.model.enums.RecommendationPriority;
import com.hemopharm.backend.repository.BloodParameterRepository;
import com.hemopharm.backend.repository.BloodTestRepository;
import com.hemopharm.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MockDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BloodParameterRepository bloodParameterRepository;
    private final BloodTestRepository bloodTestRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        BloodParameter hemoglobin = parameter("Hemoglobin",
                "Oxygen-carrying protein in red blood cells.", "g/dL");
        addRange(hemoglobin, 13.5, 17.5, "g/dL", 18, 120, Gender.MALE);
        addRange(hemoglobin, 12.0, 15.5, "g/dL", 18, 120, Gender.FEMALE);

        BloodParameter glucose = parameter("Fasting Glucose",
                "Blood sugar measured after at least 8 hours without food.", "mg/dL");
        addRange(glucose, 70.0, 99.0, "mg/dL", 18, 120, null);

        BloodParameter cholesterol = parameter("Total Cholesterol",
                "Total amount of cholesterol in the blood.", "mg/dL");
        addRange(cholesterol, 0.0, 200.0, "mg/dL", 18, 120, null);

        bloodParameterRepository.saveAll(List.of(hemoglobin, glucose, cholesterol));

        User amar = user("Amar Hodzic", "amar@example.com", "password123",
                LocalDate.of(1990, 5, 14), Gender.MALE, "+387 61 123 456", "Sarajevo");
        User lejla = user("Lejla Karic", "lejla@example.com", "password123",
                LocalDate.of(1985, 11, 2), Gender.FEMALE, "+387 62 654 321", "Mostar");
        userRepository.saveAll(List.of(amar, lejla));

        BloodTest amarTest = bloodTest(amar, "amar-blood-test-2026-11.pdf", 3);
        result(amarTest, hemoglobin, 14.2, "g/dL");
        result(amarTest, glucose, 105.0, "mg/dL");
        result(amarTest, cholesterol, 190.0, "mg/dL");

        Analysis amarAnalysis = analysis(amarTest, 3,
                "Mildly elevated fasting glucose. Hemoglobin and total cholesterol are within range.");
        recommendation(amarAnalysis, "Reduce added sugar and refined carbohydrates.", "DIET", RecommendationPriority.HIGH);
        recommendation(amarAnalysis, "Repeat fasting glucose in 3 months.", "FOLLOW_UP", RecommendationPriority.MEDIUM);

        BloodTest lejlaTest = bloodTest(lejla, "lejla-blood-test-2026-10.pdf", 30);
        result(lejlaTest, hemoglobin, 11.8, "g/dL");
        result(lejlaTest, glucose, 88.0, "mg/dL");
        result(lejlaTest, cholesterol, 205.0, "mg/dL");

        Analysis lejlaAnalysis = analysis(lejlaTest, 30,
                "Mild anemia and slightly elevated total cholesterol.");
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

    private void addRange(BloodParameter parameter, double min, double max, String unit,
                          int minAge, int maxAge, Gender gender) {
        ReferenceRange range = new ReferenceRange();
        range.setParameter(parameter);
        range.setMinValue(min);
        range.setMaxValue(max);
        range.setUnit(unit);
        range.setMinAge(minAge);
        range.setMaxAge(maxAge);
        range.setGender(gender);
        parameter.getReferenceRanges().add(range);
    }

    private User user(String name, String email, String password, LocalDate dateOfBirth,
                      Gender gender, String phone, String address) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setDateOfBirth(dateOfBirth);
        profile.setGender(gender);
        profile.setPhone(phone);
        profile.setAddress(address);
        user.setProfile(profile);
        return user;
    }

    private BloodTest bloodTest(User user, String fileName, int daysAgo) {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setUser(user);
        bloodTest.setFileName(fileName);
        bloodTest.setUploadDate(LocalDateTime.now().minusDays(daysAgo));
        bloodTest.setStatus(BloodTestStatus.COMPLETED);
        return bloodTest;
    }

    private void result(BloodTest bloodTest, BloodParameter parameter, double value, String unit) {
        BloodTestResult result = new BloodTestResult();
        result.setBloodTest(bloodTest);
        result.setParameter(parameter);
        result.setValue(value);
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
