package com.eventdriven.healthcare.patientcheckin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMetricsEvent {
    private String nfcId;
    private Double height;
    private Double weight;
    private Double insulinSensitivityFactor;
    private Instant timestamp;
}