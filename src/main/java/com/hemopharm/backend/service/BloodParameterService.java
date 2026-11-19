package com.hemopharm.backend.service;

import com.hemopharm.backend.model.BloodParameter;

import java.util.List;

public interface BloodParameterService {

    List<BloodParameter> getParameters();

    BloodParameter getParameter(Long id);
}
