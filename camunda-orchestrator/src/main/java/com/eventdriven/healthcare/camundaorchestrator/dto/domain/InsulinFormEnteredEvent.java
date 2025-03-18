package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InsulinFormEnteredEvent {
    private float nextMealCarbohydrates;
    private float insulinToCarbohydrateRatio; // Insulin-to-Carbohydrate Ratio
    // (grams/unit)
    private float targetBloodGlucoseLevel; // Target blood glucose level (mg/dL)
}