package com.eventdriven.healthcare.patientdashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    private int patientID;
    private String name;
    private String firstname;
    private float height;
    private float weight;
    private float bloodGlucose;
    private String nfcID;
    private float insulinSensitivityFactor;
}