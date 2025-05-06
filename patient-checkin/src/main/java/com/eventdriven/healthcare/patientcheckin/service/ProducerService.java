package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.dto.PatientDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    public ProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPatientDataEvent(String key, PatientDataEvent event) {
        Message<PatientDataEvent> message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "patientData")
                .setHeader(KafkaHeaders.KEY, key)
                .build();

        kafkaTemplate.send(message);
        logger.info("**** -> Published EVENT patientCheckIn: {}", message);
    }
}