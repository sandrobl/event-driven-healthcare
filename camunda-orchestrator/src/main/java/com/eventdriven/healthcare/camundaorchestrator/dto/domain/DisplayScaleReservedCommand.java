package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.Data;

@Data
public class DisplayScaleReservedCommand {
    // Indicates that the scale is reserved; always true.
    private boolean scaleReserved;
}
