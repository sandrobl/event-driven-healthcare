package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayInsulinDoseCommand;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayPatientCommand;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("displayInsulinDoseDelegate")
@RequiredArgsConstructor
public class DisplayInsulinDoseDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Override
    public void execute(DelegateExecution execution) {
        // Retrieve patient data from process variables
        String correlationId = execution.getProcessBusinessKey();
        Float insulinDose = (Float) execution.getVariable("insulin_doses");

        DisplayInsulinDoseCommand displayInsulinDoseCommand = new DisplayInsulinDoseCommand();
        displayInsulinDoseCommand.setInsulinDoses(insulinDose);

        Message<DisplayInsulinDoseCommand> message =
                MessageBuilder.withPayload(displayInsulinDoseCommand)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "displayInsulinDose")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();
        log.info("Sending message to patientEventsTopic - displayInsulinDose " +
                ": {}", message);
        kafkaTemplate.send(message);
    }
}