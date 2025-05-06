package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An event stating that the patient check-in has completed.
 * The 'found' flag indicates if the patient was in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDataEvent {
    private String nfcId;
    private boolean found;        // true if patient was found, false otherwise
    private Patient patient;
}
