package com.eventdriven.healthcare.patientdashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InsulinFormEnteredEvent {
    private float carbs;
    private float insulinRatio;
}