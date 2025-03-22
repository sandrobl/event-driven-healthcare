package com.eventdriven.healthcare.patientdashboard.model;


public enum ProcessStep {
    INFORMATION_NEEDED,    // Waiting for patient to enter data
    FORM_SUBMITTED,        // Form submitted by the user
    INSULIN_CALCULATED,   // Show the calculated insulin dose
    NO_INSULIN_NEEDED,    // No insulin needed
    SCALE_DISPLAYED,       // Show the insulin dose on the scale
    AWAITING_CONFIRMATION,   // Check if insulin dose was injected
    CONFIRMED,             // End of the process
    ERROR,                 // Error occurred
}
