package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.model.Patient;
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
                String nfcID = rootNode.get("ID").asText();

                logger.info("**** -> NFC Tag scanned with ID :: {}",
                        nfcID);

                // TODO: Should use NFC Tag ID to get patient information
                Patient patient = patientService.getPatientById(1);
                logger.info("**** -> Found:: {}",patient);

            }
        }catch (Exception e){
            logger.error("Error while processing healthCareEvent :: {}",e.getMessage());
        }
    }

}
