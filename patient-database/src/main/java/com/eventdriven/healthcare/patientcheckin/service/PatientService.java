package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import com.eventdriven.healthcare.patientcheckin.model.PatientDatabase;
import org.springframework.stereotype.Service;
import java.util.List;

@Service  // Marks this as a service managed by Spring
public class PatientService {
    private final PatientDatabase patientRepository;

    // Constructor-based Dependency Injection
    public PatientService(PatientDatabase patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getPatients() {
        return patientRepository.getPatientList();
    }

    public void addPatient(Patient patient) {
        patientRepository.addPatient(patient);
    }

    public Patient getPatientById(int id) {
        return patientRepository.getPatientById(id);
    }
}