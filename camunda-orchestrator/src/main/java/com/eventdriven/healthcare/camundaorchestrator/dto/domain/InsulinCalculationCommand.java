package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsulinCalculationCommand {
    private float nextMealCarbohydrates;
    private float insulinToCarbohydrateRatio; // Insulin-to-Carbohydrate Ratio
    // (grams/unit)
    private float targetBloodGlucoseLevel; // Target blood glucose level (mg/dL)
    private float bloodGlucose; // Current blood glucose level (mg/dL)
    private float patientInsulinSensitivityFactor; // Patient’s insulin sensitivity factor
}

