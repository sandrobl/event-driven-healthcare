package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.dto.DisplayPatientCommand;
import com.eventdriven.healthcare.patientdashboard.dto.InsulinCalculatedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerService {


    private final DashboardService dashboardService;

    /**
     * Listen for messages on the patientEventsTopic.
     * We assume these are “commands” from the orchestrator.
     */
    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeDisplayPatientDataRequest(@Payload JsonNode payload,
                                          @Header("messageCategory") String messageCategory,
                                          @Header("messageType") String messageType,
                                          @Header(KafkaHeaders.RECEIVED_KEY) String correlationId) {
        if ("COMMAND".equals(messageCategory) && "displayPatientData".equals(messageType)) {
            try {
                DisplayPatientCommand command = new ObjectMapper().treeToValue(payload, DisplayPatientCommand.class);


                log.info("Received command from Kafka: key={} value={}", correlationId, command);

                dashboardService.handleDisplayPatientCommand(correlationId, command);
            } catch (Exception e) {
                log.error("Error processing message", e);
            }
        }
    }

    /**
     * Listen for messages on the patientEventsTopic.
     * We assume these are “commands” from the orchestrator.
     */
    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInsulinCalculatedRequest(@Payload JsonNode payload,
                                                 @Header("messageCategory") String messageCategory,
                                                 @Header("messageType") String messageType,
                                                 @Header(KafkaHeaders.RECEIVED_KEY) String correlationId) {
        if ("COMMAND".equals(messageCategory) && "displayInsulinDose".equals(messageType)) {
            try {
                InsulinCalculatedEvent command = new ObjectMapper().treeToValue(payload,
                        InsulinCalculatedEvent.class);

                log.info("Received consumeInsulinCalculatedRequest from Kafka: key={} value={}",
                        correlationId, command);

                dashboardService.handleInsulinDoseCalculatedEvent(correlationId, command);
            } catch (Exception e) {
                log.error("Error processing message", e);
            }
        } else if ("COMMAND".equals(messageCategory) && "displayNoInsulinDose".equals(messageType)) {
            try {

                log.info("Received displayNoInsulinDose from Kafka: key={}",
                        correlationId );

                dashboardService.handleNoInsulinDoseEventCommand(correlationId);
            } catch (Exception e) {
                log.error("Error processing message", e);
            }
        }
    }

}
