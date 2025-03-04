package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerPatientFactory",
            groupId = "group_id")
    public void consumePatientEvent(@Payload Patient patientEvent,
                                    @Header("type") String messageType) {
        if ("patientDataRequest".equals(messageType)) {
            logger.info("**** -> Patient-Checkin Consumed patientDataRequest " +
                            "event :: {}",
                    patientEvent.toString());

        }
    }

}
