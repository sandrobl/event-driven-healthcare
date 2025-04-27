package com.eventdriven.healthcare.streamprocessor.util;

public class ScaleFormatter {
    /**
     * Accepts weights like "20"
     * and returns "weight minus the weight of the syringe (5gr)"
     */
    public static int format(int raw) {
        return raw - 4;
    }
}
