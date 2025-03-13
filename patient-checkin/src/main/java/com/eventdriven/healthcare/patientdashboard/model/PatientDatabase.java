package com.eventdriven.healthcare.patientdashboard.model;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PatientDatabase {
    public final List<Patient> patients = new ArrayList<Patient>();

    public List<Patient> getPatientList(){
        return patients;
    }

    public Patient addPatient(Patient patient) {
        patients.add(patient);
        return patient;
    }

    public Patient updatePatient(Patient patient) {
        for (Patient p : patients) {
            if (p.patientID == patient.patientID) {
                p.name = patient.name;
                p.firstname = patient.firstname;
                p.height = patient.height;
                p.weight = patient.weight;
                p.bloodGlucose = patient.bloodGlucose;
                return p;
            }
        }
        return patient;
    }

    public PatientDatabase() {
        patients.add(new Patient(1,"Fredson", "Fred", 155, 84, 5.5f, 1));
        patients.add(new Patient(2,"Johanson", "Johan", 168, 50, 4.5f, 2));
        patients.add(new Patient(3,"Hansdotir", "Hans", 182, 76, 6.5f, 3));
        patients.add(new Patient(4,"Freddotir", "Fredine", 190, 95, 1.5f, 4));
        patients.add(new Patient(5,"Johandotir", "Johanne", 191, 79, 4.5f, 5));
    }

    public Patient getPatientById(int id) {
        for (Patient patient : patients) {
            if (patient.patientID == id) {
                return patient;
            }
        }
        return null;
    }

}
