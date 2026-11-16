package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodTest;
import com.hemopharm.backend.repository.BloodTestRepository;
import com.hemopharm.backend.service.BloodTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BloodTestServiceImpl implements BloodTestService {

    private final BloodTestRepository bloodTestRepository;

    @Override
    public List<BloodTest> getBloodTests() {
        return bloodTestRepository.findAll();
    }

    @Override
    public BloodTest getBloodTest(Long id) {
        return bloodTestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blood test " + id + " not found"));
    }
}
