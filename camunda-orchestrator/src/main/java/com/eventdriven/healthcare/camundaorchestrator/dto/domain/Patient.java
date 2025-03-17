package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    int patientID;
    String name;
    String firstname;
    float height;
    float weight;
    float bloodGlucose;
    String nfcID;

    public Patient() {}

    public Patient(int patientID){
        this.patientID = patientID;
    }

}