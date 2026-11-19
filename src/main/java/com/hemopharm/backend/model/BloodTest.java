package com.hemopharm.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hemopharm.backend.model.enums.BloodTestStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "blood_tests")
public class BloodTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fileName;

    private String laboratoryName;

    private LocalDateTime testDate;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodTestStatus status = BloodTestStatus.PENDING;

    @OneToMany(mappedBy = "bloodTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BloodTestResult> results = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "bloodTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Analysis> analyses = new ArrayList<>();
}
