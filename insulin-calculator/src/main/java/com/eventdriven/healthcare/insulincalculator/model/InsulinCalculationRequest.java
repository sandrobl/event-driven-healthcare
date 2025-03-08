package com.eventdriven.healthcare.insulincalculator.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class InsulinCalculationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    int patientID;
    float bloodGlucose;
    float weight;
    float height;
    float insulinDoses;
    float nextMealCarbohydrates;
    float insulinToCarbohydrateRatio; // Insulin-to-Carbohydrate Ratio (grams/unit)
    float insulinSensitivityFactor; // Insulin Sensitivity Factor (mg/dL per unit)
    float targetBloodGlucoseLevel; // Target blood glucose level (mg/dL)

    // Parameterized constructor
    public InsulinCalculationRequest(int patientID, float bloodGlucose, float weight, float height, float nextMealCarbohydrates, float insulinToCarbohydrateRatio, float insulinSensitivityFactor, float targetBloodGlucoseLevel) {
        this.patientID = patientID;
        this.bloodGlucose = bloodGlucose;
        this.weight = weight;
        this.height = height;
        this.nextMealCarbohydrates = nextMealCarbohydrates;
        this.insulinToCarbohydrateRatio = insulinToCarbohydrateRatio;
        this.insulinSensitivityFactor = insulinSensitivityFactor;
        this.targetBloodGlucoseLevel = targetBloodGlucoseLevel;
    }

    @Override
    public String toString() {
        return "patientID: " + patientID +
                ", bloodGlucose: " + bloodGlucose +
                ", weight: " + weight +
                ", height: " + height +
                ", insulinDoses: " + insulinDoses +
                ", nextMealCarbohydrates: " + nextMealCarbohydrates +
                ", insulinToCarbohydrateRatio: " + insulinToCarbohydrateRatio +
                ", insulinSensitivityFactor: " + insulinSensitivityFactor +
                ", targetBloodGlucoseLevel: " + targetBloodGlucoseLevel;
    }
}
