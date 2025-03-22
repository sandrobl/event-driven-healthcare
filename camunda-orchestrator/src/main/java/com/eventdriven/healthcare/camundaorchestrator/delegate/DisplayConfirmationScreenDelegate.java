package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayConfirmationScreenCommand;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("displayConfirmationScreenDelegate")
@RequiredArgsConstructor
public class DisplayConfirmationScreenDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Override
    public void execute(DelegateExecution execution) {
        String correlationId = execution.getProcessBusinessKey();
        DisplayConfirmationScreenCommand command = new DisplayConfirmationScreenCommand();
        command.setMessage("Please confirm that you have injected the insulin dose.");
        Message<DisplayConfirmationScreenCommand> message = MessageBuilder.withPayload(command)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "displayConfirmationScreen")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();
        kafkaTemplate.send(message);
    }
}