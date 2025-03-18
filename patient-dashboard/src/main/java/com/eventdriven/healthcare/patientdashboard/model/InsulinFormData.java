package com.eventdriven.healthcare.patientdashboard.model;

import lombok.Data;

@Data
public class InsulinFormData {
    // e.g., user might input carbs, insulin ratio, etc.
    private float carbs;
    private float insulinRatio;
}

