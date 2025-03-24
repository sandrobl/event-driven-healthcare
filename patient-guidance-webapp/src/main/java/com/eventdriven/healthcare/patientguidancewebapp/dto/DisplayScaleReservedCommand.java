package com.eventdriven.healthcare.patientguidancewebapp.dto;

import lombok.Data;

@Data
public class DisplayScaleReservedCommand {
    // Indicates that the scale is reserved; always true.
    private boolean scaleReserved;
}
