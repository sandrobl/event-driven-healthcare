package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayErrorCommand;
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
@Component("cancelRequestInsulinCalculationRESTDelegate")
@RequiredArgsConstructor
public class CancelRequestInsulinCalculationRESTDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Override
    public void execute(DelegateExecution execution) {
        log.info("--- Canceling insulin calculation request ---");

        String correlationId = execution.getProcessBusinessKey();

        String errorMessage = "Insulin calculation could not be completed. The calculation service was unavailable. Please try Again later.";

        DisplayErrorCommand command = new DisplayErrorCommand(errorMessage);

        Message<DisplayErrorCommand> message = MessageBuilder.withPayload(command)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "displayError")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        kafkaTemplate.send(message);
    }
}