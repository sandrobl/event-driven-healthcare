package com.eventdriven.healthcare.patientdashboard.model;

import lombok.Data;

@Data
public class InsulinFormData {
    // e.g., user might input carbs, insulin ratio, etc.
    private float nextMealCarbohydrates;
    private float insulinToCarbohydrateRatio; // Insulin-to-Carbohydrate Ratio
    // (grams/unit)
    private float targetBloodGlucoseLevel; // Target blood glucose level (mg/dL)
    private float bloodGlucose; // Current blood glucose level (mg/dL)
    private float patientInsulinSensitivityFactor; // Patient’s insulin sensitivity factor

}

