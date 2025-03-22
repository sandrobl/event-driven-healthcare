package com.eventdriven.healthcare.patientdashboard.dto;

import lombok.Data;

@Data
public class InjectionConfirmedEvent {
    // Indicates whether the injection has been confirmed by the patient.
    private boolean confirmed;
}
