package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisplayInsulinDoseCommand {
    private float insulinDoses; // Insulin doses in ml
}
