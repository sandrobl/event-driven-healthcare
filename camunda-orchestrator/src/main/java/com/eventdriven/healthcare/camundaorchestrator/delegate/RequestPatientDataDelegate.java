package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.CheckInCommand;
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

@Component("requestPatientDataDelegate")
@RequiredArgsConstructor
public class RequestPatientDataDelegate implements JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;


    @Override
    public void execute(DelegateExecution execution) {
        // Possibly get 'nfcId' from process variables
        String nfcId = (String) execution.getVariable("patient_nfcId");
        String location = (String) execution.getVariable("nfc_location");
        String messageId = (String) execution.getVariable("nfc_messageId");
        String correlationId = execution.getProcessBusinessKey();



        // Build a JSON payload or command object
        CheckInCommand cmd = new CheckInCommand();
        cmd.setNfcId(nfcId);
        cmd.setMessageId(messageId);
        cmd.setLocation(location);
        cmd.setTimestamp(new Date());

        Message<CheckInCommand> message = MessageBuilder.withPayload(cmd)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "getPatientData")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        // Send the message
        kafkaTemplate.send(message);
        logger.info("**** -> Published COMMAND getPatientData: {}", message);


        // Optionally store something back into process variables
        //execution.setVariable("commandSent", true);
    }

}
