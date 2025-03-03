package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Service
public class ConsumerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PatientService patientService;

    public ConsumerService(PatientService patientService) {
        this.patientService = patientService;
    }

    @KafkaListener(
            topics = {"${spring.kafka.clickEvents-topic}"},
            containerFactory = "kafkaListenerPatientFactory",
            groupId = "group_id")
    public void consumeClickEvent(@Payload Patient patientEvent,
                                  @Header("type") String messageType) {

        if ("patientDataRequest".equals(messageType)) {
            logger.info("**** -> Consumed patientDataRequest event :: {}",patientEvent);
            logger.info("**** -> Get Patient by ID :: {}",patientEvent.getPatientID());

            Patient patient = patientService.getPatientById(patientEvent.getPatientID());
            logger.info("**** -> Found:: {}",patient);
            return;
        }else{
            logger.info("**** -> Consumed gaze event :: {}", patientEvent);
        }
    }

}
