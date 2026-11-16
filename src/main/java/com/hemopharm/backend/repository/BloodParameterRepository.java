package com.hemopharm.backend.repository;

import com.hemopharm.backend.model.BloodParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BloodParameterRepository extends JpaRepository<BloodParameter, Long> {

    Optional<BloodParameter> findByNameIgnoreCase(String name);
}
