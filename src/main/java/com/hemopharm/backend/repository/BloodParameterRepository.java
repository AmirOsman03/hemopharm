package com.hemopharm.backend.repository;

import com.hemopharm.backend.model.BloodParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodParameterRepository extends JpaRepository<BloodParameter, Long> {
}
