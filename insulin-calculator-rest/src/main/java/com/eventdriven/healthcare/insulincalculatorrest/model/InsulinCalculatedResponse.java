package com.eventdriven.healthcare.insulincalculatorrest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsulinCalculatedResponse {
    private float insulinDoses; // Insulin doses in ml
    private boolean insulinRequired;
    private String errorCode;
}
