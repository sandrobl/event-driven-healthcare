package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsulinCalculatedEvent {
    private float insulinDoses; // Insulin doses in ml
    private boolean insulinRequired;
}
