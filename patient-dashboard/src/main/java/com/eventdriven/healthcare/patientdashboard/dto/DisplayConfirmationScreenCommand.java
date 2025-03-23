package com.eventdriven.healthcare.patientdashboard.dto;

import lombok.Data;

@Data
public class DisplayConfirmationScreenCommand {
    // A message to be shown on the confirmation screen.
    private String message;
}
