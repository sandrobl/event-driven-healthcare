
package com.eventdriven.healthcare.streamprocessor.serialization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedPatient {
    private String nfcId;
    private String name;
    private String firstname;
    private Double height;
    private Double weight;
    private Double insulinSensitivityFactor;
    private Instant timestamp;
    /**
     * Factory to merge static + metrics
     */
    public static EnrichedPatient from(
            PatientStaticEvent stat,
            PatientMetricsEvent met
    ) {
        EnrichedPatient p = new EnrichedPatient();
        p.setNfcId(stat.getNfcId());
        p.setName(stat.getName());
        p.setFirstname(stat.getFirstname());
        if (met != null) {
            p.setHeight(met.getHeight());
            p.setWeight(met.getWeight());
            p.setInsulinSensitivityFactor(met.getInsulinSensitivityFactor());
            p.setTimestamp(met.getTimestamp());
        }
        return p;
    }
}
