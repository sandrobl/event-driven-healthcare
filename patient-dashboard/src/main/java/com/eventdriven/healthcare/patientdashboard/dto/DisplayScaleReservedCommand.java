package com.eventdriven.healthcare.patientdashboard.dto;

import lombok.Data;

@Data
public class DisplayScaleReservedCommand {
    // Indicates that the scale is reserved; always true.
    private boolean scaleReserved;
}
