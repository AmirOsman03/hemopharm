package com.hemopharm.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "blood_test_results")
public class BloodTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "blood_test_id", nullable = false)
    private BloodTest bloodTest;

    @ManyToOne
    @JoinColumn(name = "parameter_id", nullable = false)
    private BloodParameter parameter;

    @Column(name = "result_value", nullable = false)
    private Double value;

    private String unit;
}
