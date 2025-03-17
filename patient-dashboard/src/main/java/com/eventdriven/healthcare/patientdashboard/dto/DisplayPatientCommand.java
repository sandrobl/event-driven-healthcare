package com.eventdriven.healthcare.patientdashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisplayPatientCommand {
    private Patient patient;
}