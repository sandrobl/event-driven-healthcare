package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayNoInsulinDoseCommand;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("displayNoInsulinDoseDelegate")
@RequiredArgsConstructor
public class DisplayNoInsulinDoseDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Override
    public void execute(DelegateExecution execution) {
        // Retrieve patient data from process variables
        String correlationId = execution.getProcessBusinessKey();

        DisplayNoInsulinDoseCommand command = new DisplayNoInsulinDoseCommand();
        command.setInsulinRequired(false);

        Message<DisplayNoInsulinDoseCommand> message =
                MessageBuilder.withPayload(command)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "displayNoInsulinDose")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        kafkaTemplate.send(message);
    }
}