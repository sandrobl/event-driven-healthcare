package com.eventdriven.healthcare.patientdashboard.dto;

import lombok.Data;

@Data
public class DisplayIncorrectDoseCommand {
    // Indicates that the measured dose is incorrect.
    private boolean doseCorrect; // should be false
    // The difference between the expected and measured dose.
    private float doseDifference;
}