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
        int patientId = (int) execution.getVariable("patient_id");
        String patientFirstName = (String) execution.getVariable("patient_FirstName");
        String patientLastName = (String) execution.getVariable("patient_LastName");
        String patientNfcId = (String) execution.getVariable("patient_nfcId");
        Float patientHeight = (Float) execution.getVariable("patient_height");
        Float patientWeight = (Float) execution.getVariable("patient_weight");
        Float insulinSensitivityFactor = (Float) execution.getVariable("patient_insulinSensitivityFactor");
        String correlationId = execution.getProcessBusinessKey();


        Patient patient = new Patient(
                patientId,
                patientLastName,
                patientFirstName,
                patientHeight,
                patientWeight,
                patientNfcId,
                insulinSensitivityFactor

        );
        // Build the command object. The command does not include the "found" flag.
        DisplayPatientCommand command = new DisplayPatientCommand();
        command.setPatient(patient);
        // You can add additional fields if needed.

        // Build a message with headers:
        // - messageCategory: "COMMAND" (general)
        // - messageType: "displayPatientData" (specific type)
        // - KEY: correlationId
        Message<DisplayPatientCommand> message = MessageBuilder.withPayload(command)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "displayPatientData")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        kafkaTemplate.send(message);
    }
}