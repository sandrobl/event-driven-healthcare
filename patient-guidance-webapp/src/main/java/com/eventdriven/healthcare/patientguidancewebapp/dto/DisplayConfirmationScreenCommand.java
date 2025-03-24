package com.eventdriven.healthcare.patientguidancewebapp.dto;

import lombok.Data;

@Data
public class DisplayConfirmationScreenCommand {
    // A message to be shown on the confirmation screen.
    private String message;
}
