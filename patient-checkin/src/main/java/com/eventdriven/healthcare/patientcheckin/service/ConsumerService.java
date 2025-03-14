package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @KafkaListener(
            topics = {"${spring.kafka.mqttEvents-topic}"},
            containerFactory = "kafkaListenerPatientFactory",
            groupId = "group_id")
    public void consumeHealthCareDate(@Payload JsonNode healthCareEvent) {

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(healthCareEvent.toString());
            String type = rootNode.get("type").asText();

            if("nfc".equalsIgnoreCase(type)){
                String UID = rootNode.get("UID").asText();
                String location = rootNode.get("location").asText();
                String messageID = rootNode.get("messageID").asText();
                int readingId =
                        Integer.parseInt(rootNode.get("readingID").asText());

                if(readingId == 0){
                    return;
                }
                String rawNfcID = rootNode.get("ID").asText();

                // Format NFC ID before database lookup
                String formattedNfcID = formatNfcId(rawNfcID);

                logger.info("**** -> NFC Tag scanned with formatted ID :: {}", formattedNfcID);

                // Look up patient by formatted NFC ID
                Patient patient = patientService.getPatientByNfcId(formattedNfcID);

                if (patient != null) {
                    logger.info("**** -> Found patient: {}", patient);
                } else {
                    logger.warn("**** -> No patient found for NFC ID: {}", formattedNfcID);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing NFC event: ", e);
        }
    }

    /**
     * Converts an NFC ID from event format (e.g., "ID 0x04 0xDA 0xF2 ...")
     * into a format that matches the database (e.g., "04DAF28AB45780").
     */
    private String formatNfcId(String rawNfcId) {
        if (rawNfcId == null || !rawNfcId.startsWith("ID ")) {
            return null; // Invalid format
        }
        return rawNfcId.replace("ID ", "") // Remove "ID " prefix
                .replaceAll("0x", "") // Remove "0x" prefixes
                .replaceAll(" ", ""); // Remove spaces
    }
}
