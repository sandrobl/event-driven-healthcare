package com.eventdriven.healthcare.patientdashboard.model;

import com.eventdriven.healthcare.patientdashboard.dto.InsulinCalculatedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the “state” of one process instance on the dashboard:
 * - correlationId: the unique key from Kafka / Camunda
 * - patient: the patient info
 * - currentStep: e.g., “displayPatientData”, “askForInsulinDose”, “awaitConfirmation”, etc.
 * - anything else you want to show in the UI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardProcessInfo {
    private String correlationId;
    private Patient patient;
    private ProcessStep currentStep;
    private InsulinCalculatedEvent insulinCalculatedEvent;
}
