package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.model.InsulinCalculationRequest;
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


    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerInsulinCalculationFactory",
            groupId = "group_id")
    public void consumePatientEvent(@Payload InsulinCalculationRequest patientEvent,
                                    @Header("type") String messageType) {
        if ("insulinCalculationRequest".equals(messageType)) {
            logger.info("**** -> patient-dashboard Consumed " +
                            "patientDataRequest insulincalculationrequest" +
                            "event :: {}",
                    patientEvent.toString());

            // So now wait for the scale to display the correct

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

            // {"type": "load_cell", "UID": "23xq", "location": "HYGIENE_STATION", "messageID": 546, "weight": -3}
            if("load_cell".equalsIgnoreCase(type)){
                String UID = rootNode.get("UID").asText();
                String location = rootNode.get("location").asText();
                String messageID = rootNode.get("messageID").asText();

                float weight =
                        Integer.parseInt(rootNode.get("weight").asText());

                if(weight == 0){
                    return;
                }

                // Pass this weight measurement to the current patient logged
                // in. It should match what the patient received from the
                // insulin calculator


            }
        }catch (Exception e){
            logger.error("Error while processing healthCareEvent :: {}",e.getMessage());
        }
    }

}
