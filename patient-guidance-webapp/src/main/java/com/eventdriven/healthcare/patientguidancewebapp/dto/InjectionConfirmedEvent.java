package com.eventdriven.healthcare.patientguidancewebapp.dto;

import lombok.Data;

@Data
public class InjectionConfirmedEvent {
    // Indicates whether the injection has been confirmed by the patient.
    private boolean confirmed;
}
