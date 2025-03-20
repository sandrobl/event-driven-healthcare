package com.eventdriven.healthcare.patientdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class InsulinCalculatedEvent {
    private float insulinDoses;
}
