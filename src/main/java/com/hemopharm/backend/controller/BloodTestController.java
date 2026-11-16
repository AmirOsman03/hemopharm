package com.hemopharm.backend.controller;

import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.service.BloodTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blood-tests")
@RequiredArgsConstructor
public class BloodTestController {

    private final BloodTestService bloodTestService;

    @GetMapping
    public List<BloodTest> getBloodTests() {
        return bloodTestService.getBloodTests();
    }

    @GetMapping("/{id}")
    public BloodTest getBloodTest(@PathVariable Long id) {
        return bloodTestService.getBloodTest(id);
    }
}
