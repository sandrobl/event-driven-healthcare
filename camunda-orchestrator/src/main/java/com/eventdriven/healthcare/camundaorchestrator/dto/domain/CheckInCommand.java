package com.eventdriven.healthcare.camundaorchestrator.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * A command to request patient data or confirm check-in.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInCommand {
    private String nfcId;
    private String messageId;
    private String location;
    private Date timestamp;
}
