package com.eventdriven.healthcare.insulincalculator.dto;

import com.eventdriven.healthcare.insulincalculator.model.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsulinCalculationCommand {
    private float insulinDoses; // Insulin doses in ml
    private float nextMealCarbohydrates;
    private float insulinToCarbohydrateRatio; // Insulin-to-Carbohydrate Ratio
    // (grams/unit)
    private float targetBloodGlucoseLevel; // Target blood glucose level (mg/dL)
    private float bloodGlucose; // Current blood glucose level (mg/dL)
    private float patientInsulinSensitivityFactor; // Insulin Sensitivity Factor (mg/dL/unit)
}

