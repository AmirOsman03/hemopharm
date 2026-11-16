package com.hemopharm.backend.controller;

import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.service.BloodParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
public class BloodParameterController {

    private final BloodParameterService bloodParameterService;

    @GetMapping
    public List<BloodParameter> getParameters() {
        return bloodParameterService.getParameters();
    }

    @GetMapping("/{id}")
    public BloodParameter getParameter(@PathVariable Long id) {
        return bloodParameterService.getParameter(id);
    }
}
