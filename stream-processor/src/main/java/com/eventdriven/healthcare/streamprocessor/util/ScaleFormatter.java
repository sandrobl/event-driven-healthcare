package com.eventdriven.healthcare.streamprocessor.util;

public class ScaleFormatter {
    /**
     * Accepts weights like "20"
     * and returns "weight minus the weight of the syringe (5gr)"
     */
    public static float format(float raw) {
        return raw - 4f;
    }
}
