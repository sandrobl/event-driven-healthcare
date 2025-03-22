package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.Data;

@Data
public class DisplayIncorrectDoseCommand {
    // Indicates that the measured dose is incorrect.
    private boolean doseCorrect; // should be false
    // The difference between the expected and measured dose.
    private float doseDifference;
}