package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    // Primary / static patient info (from DB)
    private Integer patientID;
    private String name;
    private String firstname;
    private String nfcID;
    private String address;
    private String city;
    private String plz;
    private LocalDate dateOfBirth;

    // Dynamic vitals (populated from Kafka)
    // use boxed types so they can be null until set
    private Float height;
    private Float weight;
    private Float insulinSensitivityFactor;
}