package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.Data;

@Data
public class InjectionConfirmedEvent {
    // Indicates whether the injection has been confirmed by the patient.
    private boolean confirmed;
}
