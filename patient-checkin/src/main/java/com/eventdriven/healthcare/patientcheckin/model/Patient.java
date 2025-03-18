package com.eventdriven.healthcare.patientcheckin.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    int patientID;
    String name;
    String firstname;
    float height;
    float weight;
    float bloodGlucose;
    String nfcID;
    float insulinSensitivityFactor;

    public Patient() {}
    public Patient(int patientID, String name, String firstname, float height
            , float weight, float bloodGlucose, String nfcID,float insulinSensitivityFactor) {
        this.patientID = patientID;
        this.name = name;
        this.firstname = firstname;
        this.height = height;
        this.weight = weight;
        this.bloodGlucose = bloodGlucose;
        this.nfcID = nfcID;
        this.insulinSensitivityFactor = insulinSensitivityFactor;
    }

    public Patient(int patientID){
        this.patientID = patientID;
    }

    @Override
    public String toString()
    {
        return "patientID: " + patientID +
                ", name: " + name +
                ", firstname: " + firstname +
                ", height: " + height +
                ", weight: " + weight +
                ", bloodGlucose: " + bloodGlucose +
                ", nfcID: " + nfcID +
                ", insulinSensitivityFactor: " + insulinSensitivityFactor;
    }
}