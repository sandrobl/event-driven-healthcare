package com.eventdriven.healthcare.scale.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScaleReservationRequest {
    private String correlationId;
}