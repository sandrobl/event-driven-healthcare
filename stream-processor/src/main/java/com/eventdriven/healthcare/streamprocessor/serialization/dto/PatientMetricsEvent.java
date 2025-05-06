package com.eventdriven.healthcare.streamprocessor.serialization.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
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