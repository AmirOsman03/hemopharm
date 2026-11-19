package com.hemopharm.backend.repository;

import com.hemopharm.backend.model.BloodTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodTestRepository extends JpaRepository<BloodTest, Long> {

    List<BloodTest> findByUserId(Long userId);
}
