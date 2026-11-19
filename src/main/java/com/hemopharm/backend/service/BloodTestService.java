package com.hemopharm.backend.service;

import com.hemopharm.backend.model.BloodTest;

import java.util.List;

public interface BloodTestService {

    List<BloodTest> getBloodTests();

    BloodTest getBloodTest(Long id);
}
