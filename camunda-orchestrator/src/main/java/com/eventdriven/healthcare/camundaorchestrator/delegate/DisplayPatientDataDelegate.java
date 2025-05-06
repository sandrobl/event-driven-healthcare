// File: src/main/java/com/eventdriven/healthcare/camundaorchestrator/delegate/DisplayPatientDataDelegate.java
package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayPatientCommand;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.Patient;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("displayPatientDataDelegate")
@RequiredArgsConstructor
public class DisplayPatientDataDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Override
    public void execute(DelegateExecution execution) {
        // Retrieve patient data from process variables
        Integer patientId = (Integer) execution.getVariable("patient_id");
        String firstName   = (String)  execution.getVariable("patient_FirstName");
        String lastName    = (String)  execution.getVariable("patient_LastName");
        String nfcId       = (String)  execution.getVariable("patient_nfcId");

        Float height = execution.getVariable("patient_height") instanceof Number
                ? ((Number)execution.getVariable("patient_height")).floatValue()
                : null;
        Float weight = execution.getVariable("patient_weight") instanceof Number
                ? ((Number)execution.getVariable("patient_weight")).floatValue()
                : null;
        Float isf = execution.getVariable("patient_insulinSensitivityFactor") instanceof Number
                ? ((Number)execution.getVariable("patient_insulinSensitivityFactor")).floatValue()
                : null;

        String correlationId = execution.getProcessBusinessKey();

        // Build the Patient DTO via Lombok builder
        Patient patient = Patient.builder()
                .patientID(patientId)
                .firstname(firstName)
                .name(lastName)
                .nfcID(nfcId)
                .height(height)
                .weight(weight)
                .insulinSensitivityFactor(isf)
                .build();

        DisplayPatientCommand command = new DisplayPatientCommand();
        command.setPatient(patient);

        Message<DisplayPatientCommand> message = MessageBuilder.withPayload(command)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "displayPatientData")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        kafkaTemplate.send(message);
    }
}