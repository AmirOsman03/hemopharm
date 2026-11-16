package com.hemopharm.backend.repository;

import com.hemopharm.backend.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
}
