package com.eventdriven.healthcare.insulincalculator.dto;

import com.eventdriven.healthcare.insulincalculator.model.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsulinCalculatedEvent {
    private Patient patient;
    private float insulinDoses; // Insulin doses in ml
    private boolean insulinRequired;
}
