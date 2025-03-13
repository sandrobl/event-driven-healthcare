package com.eventdriven.healthcare.patientdashboard.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeightMeasurement {
    private float weight;
    private String location;

    public WeightMeasurement(float weight, String location) {
        this.weight = weight;
        this.location = location;
    }
}
