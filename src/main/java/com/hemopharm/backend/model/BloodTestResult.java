package com.hemopharm.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
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

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "blood_test_id", nullable = false)
    private BloodTest bloodTest;

    @JsonIncludeProperties({"id", "name", "minLimit", "maxLimit"})
    @ManyToOne
    @JoinColumn(name = "parameter_id", nullable = false)
    private BloodParameter parameter;

    @Column(name = "result_value", nullable = false)
    private Double value;

    /** Value as written in the report, before numeric parsing. */
    private String rawValue;

    private String unit;
}
