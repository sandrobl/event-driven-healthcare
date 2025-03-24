package com.eventdriven.healthcare.patientguidancewebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class InsulinCalculatedEvent {
    private float insulinDoses;
}
