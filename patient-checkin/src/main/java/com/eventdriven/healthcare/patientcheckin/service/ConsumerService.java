package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.dto.CheckInCommand;
import com.eventdriven.healthcare.patientcheckin.model.Patient;
import com.eventdriven.healthcare.patientcheckin.dto.PatientDataEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PatientService patientService;
    private final ProducerService producerService;


    // Listen for patient data request commands on a dedicated commands topic
    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumePatientDataRequest(@Payload JsonNode payload,
                                          @Header("messageCategory") String messageCategory,
                                          @Header("messageType") String messageType,
                                          @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        if ("COMMAND".equals(messageCategory) && "getPatientData".equals(messageType)) {
            try {
                CheckInCommand command = new ObjectMapper().treeToValue(payload, CheckInCommand.class);

                logger.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, key);

                Patient patient = patientService.getPatientByNfcId(command.getNfcId());
                boolean found = (patient != null);

                PatientDataEvent event = new PatientDataEvent();
                event.setNfcId(command.getNfcId());
                event.setFound(found);
                if (found) {
                    event.setPatient(patient);
                }

                producerService.sendPatientDataEvent(key, event);
            } catch (Exception e) {
                logger.error("Error processing patient data request command: {}", e.getMessage());
            }
        }
    }

    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInsulinCalculationRequest(@Payload JsonNode payload,
                                          @Header("messageCategory") String messageCategory,
                                          @Header("messageType") String messageType,
                                          @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        if ("COMMAND".equals(messageCategory) && "getPatientData".equals(messageType)) {
            try {
                CheckInCommand command = new ObjectMapper().treeToValue(payload, CheckInCommand.class);

                logger.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, key);

                Patient patient = patientService.getPatientByNfcId(command.getNfcId());
                boolean found = (patient != null);

                PatientDataEvent event = new PatientDataEvent();
                event.setNfcId(command.getNfcId());
                event.setFound(found);
                if (found) {
                    event.setPatient(patient);
                }

                producerService.sendPatientDataEvent(key, event);
            } catch (Exception e) {
                logger.error("Error processing patient data request command: {}", e.getMessage());
            }
        }
    }

}