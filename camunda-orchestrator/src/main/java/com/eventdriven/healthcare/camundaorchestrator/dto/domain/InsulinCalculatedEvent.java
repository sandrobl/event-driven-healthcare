package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsulinCalculatedEvent {
    private Patient patient;
    private float insulinDoses; // Insulin doses in ml
    private boolean insulinRequired;
}
