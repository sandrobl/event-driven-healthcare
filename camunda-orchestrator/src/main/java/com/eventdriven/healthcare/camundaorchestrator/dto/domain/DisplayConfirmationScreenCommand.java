package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.Data;

@Data
public class DisplayConfirmationScreenCommand {
    // A message to be shown on the confirmation screen.
    private String message;
}
