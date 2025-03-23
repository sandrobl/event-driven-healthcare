package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayIncorrectDoseCommand;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("displayIncorrectDoseDelegate")
@RequiredArgsConstructor
public class DisplayIncorrectDoseDelegate implements JavaDelegate {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Override
    public void execute(DelegateExecution execution) {
        String correlationId = execution.getProcessBusinessKey();
        // Assume "doseDifference" is stored as a process variable.
        Float doseDifference = (Float) execution.getVariable("scale_validation_dose_difference");
        if (doseDifference == null) {
            doseDifference = 0f;
        }
        DisplayIncorrectDoseCommand command = new DisplayIncorrectDoseCommand();
        command.setDoseCorrect(false);
        command.setDoseDifference(doseDifference);
        Message<DisplayIncorrectDoseCommand> message = MessageBuilder.withPayload(command)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "displayIncorrectDose")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();
        kafkaTemplate.send(message);
    }
}
