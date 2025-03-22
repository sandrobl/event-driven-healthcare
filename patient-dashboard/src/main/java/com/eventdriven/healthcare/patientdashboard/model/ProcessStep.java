package com.eventdriven.healthcare.patientdashboard.model;


public enum ProcessStep {
    INFORMATION_NEEDED,    // Waiting for patient to enter data
    FORM_SUBMITTED,        // Form submitted by the user
    INSULIN_CALCULATED,   // Show the calculated insulin dose
    NO_INSULIN_NEEDED,    // No insulin needed
    SCALE_RESERVED,       // Scale reserved
    INCORRECT_DOSE,       // Show warning that the dose is incorrect
    AWAITING_CONFIRMATION,   // Check if insulin dose was injected
    CONFIRMED,             // End of the process
    ERROR,                 // Error occurred
}
