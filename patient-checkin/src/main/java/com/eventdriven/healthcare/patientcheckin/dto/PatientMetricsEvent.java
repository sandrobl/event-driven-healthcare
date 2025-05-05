package com.eventdriven.healthcare.patientcheckin.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientMetricsEvent {
    private String nfcId;
    private String name;
    private String firstname;
    private Double height;
    private Double weight;
    private Double insulinSensitivityFactor;
    private Instant timestamp;

}
