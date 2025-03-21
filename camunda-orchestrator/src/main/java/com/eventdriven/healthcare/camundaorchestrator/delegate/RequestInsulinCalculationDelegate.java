package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinCalculationCommand;
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

import java.util.Date;

@Component("requestInsulinCalculationDelegate")
@RequiredArgsConstructor
public class RequestInsulinCalculationDelegate implements JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;


    @Override
    public void execute(DelegateExecution execution) throws Exception {

        Float patientBloodGlucose = (Float) execution.getVariable("patient_bloodGlucose");
        Float insulinSensitivityFactor = (Float) execution.getVariable("patient_insulinSensitivityFactor");
        Float nextMealCarbohydrates = (Float) execution.getVariable("patient_nextMealCarbohydrates");
        Float insulinToCarbohydrateRatio = (Float) execution.getVariable("patient_insulinToCarbohydrateRatio");
        Float targetBloodGlucoseLevel = (Float) execution.getVariable("patient_targetBloodGlucoseLevel");
        String correlationId = execution.getProcessBusinessKey();

        // Build a JSON payload or command object
        InsulinCalculationCommand cmd = new InsulinCalculationCommand();
        cmd.setNextMealCarbohydrates(nextMealCarbohydrates);
        cmd.setInsulinToCarbohydrateRatio(insulinToCarbohydrateRatio);
        cmd.setTargetBloodGlucoseLevel(targetBloodGlucoseLevel);
        cmd.setBloodGlucose(patientBloodGlucose);
        cmd.setPatientInsulinSensitivityFactor(insulinSensitivityFactor);

        Message<InsulinCalculationCommand> message = MessageBuilder.withPayload(cmd)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "calculateInsulin")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        // Send the message
        kafkaTemplate.send(message);
        logger.info("**** -> Published COMMAND calculateInsulin: {}", message);

        // Optionally store something back into process variables
        //execution.setVariable("commandSent", true);
    }
}
