package com.eventdriven.healthcare.insulincalculator.service;

import com.eventdriven.healthcare.insulincalculator.dto.InsulinCalculatedEvent;
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
public class ProducerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendInsulinCalculatedRequest(InsulinCalculatedEvent ice){
        Message<InsulinCalculatedEvent> message = MessageBuilder
                .withPayload(ice)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "insulinCalculated")
                .build();

        logger.info("#### -> Publishing insulin calculated event :: " +
                "{}",ice);

        kafkaTemplate.send(message);
    }

}