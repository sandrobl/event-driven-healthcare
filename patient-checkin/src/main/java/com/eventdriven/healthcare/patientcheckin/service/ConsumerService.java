package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.model.CheckInCommand;
import com.eventdriven.healthcare.patientcheckin.model.Patient;
import com.eventdriven.healthcare.patientcheckin.model.PatientCheckInEvent;
import com.eventdriven.healthcare.patientcheckin.service.PatientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PatientService patientService;
    private final KafkaTemplate<String, PatientCheckInEvent> kafkaTemplate;

    public ConsumerService(PatientService patientService,
                           KafkaTemplate<String, PatientCheckInEvent> kafkaTemplate) {
        this.patientService = patientService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Listen for patient data request commands on a dedicated commands topic
    @KafkaListener(
            topics = {"${spring.kafka.patientCommands-topic}"},
            containerFactory = "kafkaListenerPatientFactory",
            groupId = "group_id")
    public void consumePatientDataRequest(@Payload JsonNode payload,
                                          @Header("messageCategory") String messageCategory,
                                          @Header("messageType") String messageType,
                                          @Header(KafkaHeaders.KEY) String key) {
        if ("COMMAND".equals(messageCategory) && "getPatientData".equals(messageType)) {
            try {
                CheckInCommand command = new ObjectMapper().treeToValue(payload, CheckInCommand.class);

            logger.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, key);

            // Look up the patient by the NFC ID from the command
            Patient patient = patientService.getPatientByNfcId(command.getNfcId());
            boolean found = (patient != null);

            // Build a PatientCheckInEvent containing the patient data and found flag
            PatientCheckInEvent event = new PatientCheckInEvent();
            event.setNfcId(command.getNfcId());
            event.setFound(found);
            if (found) {
                event.setPatient(patient);
            }

            Message<PatientCheckInEvent> message = MessageBuilder.withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, "${spring.kafka.patientEvents-topic}")
                    .setHeader("messageCategory", "EVENT")
                    .setHeader("messageType", "patientCheckedIn")
                    .setHeader(KafkaHeaders.KEY, key)
                    .build();

            // Publish the event to the patient events topic
            kafkaTemplate.send(message);
            logger.info("**** -> Published EVENT patientCheckIn: {}", message);
            } catch (Exception e) {
                logger.error("Error processing patient data request command: {}", e.getMessage());
            }
        }
    }
}