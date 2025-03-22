package com.eventdriven.healthcare.scale.dto;

import lombok.Data;

@Data
public class ScaleCommand {
    private Type action;
    private String correlationId;
    private Float insulinDose;
    private Float scaleValue;

    public enum Type {
        RESERVE_SCALE,
        VALIDATE_DOSE,
        UNRESERVE_SCALE
    }
}

