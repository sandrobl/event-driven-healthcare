package com.eventdriven.healthcare.camundaorchestrator.dto.camunda;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CamundaMessageDto implements Serializable {

    private String correlationId;
    private Map<String, Object> vars;

}
