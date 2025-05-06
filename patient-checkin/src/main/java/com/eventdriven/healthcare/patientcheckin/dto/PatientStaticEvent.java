package com.eventdriven.healthcare.patientcheckin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientStaticEvent {
    private String nfcId;
    private String name;
    private String firstname;
}