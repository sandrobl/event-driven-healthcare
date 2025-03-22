package com.eventdriven.healthcare.scale.dto;

import lombok.Data;

@Data
public class ScaleEvent {
    private Type type;
    /**
     * For the INSULIN_DOSE_VALIDATED event:
     * - insulinDoseValid: indicates whether the measured dose matches the requested dose.
     * - doseDifference: the difference between the measured and requested dose.
     */
    private Boolean insulinDoseValid;
    private Float doseDifference;

    public enum Type {
        SCALE_RESERVED,
        SCALE_UNRESERVED,
        INSULIN_DOSE_VALIDATED
    }
}
