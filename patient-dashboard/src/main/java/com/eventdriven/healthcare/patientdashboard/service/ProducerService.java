package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.dto.InsulinFormEnteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    public ProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInsulinFormEnteredEvent(String key, InsulinFormEnteredEvent event) {

        Message<InsulinFormEnteredEvent> message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "COMMAND")
                .setHeader("messageType", "insulinFormEntered")
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.RECEIVED_KEY, key)
                .build();

        kafkaTemplate.send(message);
        log.info("**** -> Published EVENT insulinFormEntered: {}", message);
    }
}