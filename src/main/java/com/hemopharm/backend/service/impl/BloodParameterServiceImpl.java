package com.hemopharm.backend.service.impl;

import com.hemopharm.backend.exception.NotFoundException;
import com.hemopharm.backend.model.BloodParameter;
import com.hemopharm.backend.repository.BloodParameterRepository;
import com.hemopharm.backend.service.BloodParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BloodParameterServiceImpl implements BloodParameterService {

    private final BloodParameterRepository bloodParameterRepository;

    @Override
    public List<BloodParameter> getParameters() {
        return bloodParameterRepository.findAll();
    }

    @Override
    public BloodParameter getParameter(Long id) {
        return bloodParameterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blood parameter " + id + " not found"));
    }
}
