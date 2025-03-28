package com.eventdriven.healthcare.patientguidancewebapp.service;

import com.eventdriven.healthcare.patientguidancewebapp.dto.InjectionConfirmedEvent;
import com.eventdriven.healthcare.patientguidancewebapp.dto.InsulinFormEnteredEvent;
import lombok.extern.slf4j.Slf4j;
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
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "insulinFormEntered")
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.RECEIVED_KEY, key)
                .build();

        kafkaTemplate.send(message);
        log.info("**** -> Published insulinFormEntered: {}",message);
    }

    public void sendInjectionConfirmedEvent(String key, InjectionConfirmedEvent event) {
        Message<InjectionConfirmedEvent> message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "injectionConfirmed")
                .setHeader(KafkaHeaders.KEY, key)
                .build();
        kafkaTemplate.send(message);
        log.info("Published injectionConfirmed event for key={} with confirmed={}", key, event.isConfirmed());
    }
}