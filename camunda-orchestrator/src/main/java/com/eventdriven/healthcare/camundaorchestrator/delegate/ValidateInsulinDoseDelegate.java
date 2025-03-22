package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.ScaleCommand;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("reserveScaleDelegate")
@RequiredArgsConstructor
public class ValidateInsulinDoseDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateInsulinDoseDelegate.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Override
    public void execute(DelegateExecution execution) {
        String correlationId = execution.getProcessBusinessKey();
        Float insulinDose = (Float) execution.getVariable("insulin_doses");
        Float scaleValue = (Float) execution.getVariable("latest_scale_value");

        LOG.info("Validating insulin dose on scale for correlationId: {}, insulinDose={}, scaleValue={}", correlationId, insulinDose, scaleValue);

        ScaleCommand cmd = new ScaleCommand();
        cmd.setAction(ScaleCommand.Type.VALIDATE_DOSE);
        cmd.setInsulinDose(insulinDose);
        cmd.setScaleValue(scaleValue);

        cmd.setCorrelationId(correlationId);

        Message<ScaleCommand> message = MessageBuilder
                .withPayload(cmd)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "validateInsulinDose")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        kafkaTemplate.send(message);
    }
}
