package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsulinCalculationCommand {
    private Patient patient;
    private float insulinDoses; // Insulin doses in ml
    private float nextMealCarbohydrates;
    private float insulinToCarbohydrateRatio; // Insulin-to-Carbohydrate Ratio
    // (grams/unit)
    private float targetBloodGlucoseLevel; // Target blood glucose level (mg/dL)
}

