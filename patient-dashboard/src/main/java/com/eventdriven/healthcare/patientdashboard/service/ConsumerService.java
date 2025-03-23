package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.dto.DisplayErrorCommand;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayPatientCommand;
import com.eventdriven.healthcare.patientdashboard.dto.InsulinCalculatedEvent;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayScaleReservedCommand;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayIncorrectDoseCommand;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayConfirmationScreenCommand;
import com.eventdriven.healthcare.patientdashboard.dto.InjectionConfirmedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeDashboardCommands(@Payload JsonNode payload,
                                         @Header("messageCategory") String messageCategory,
                                         @Header("messageType") String messageType,
                                         @Header(KafkaHeaders.RECEIVED_KEY) String correlationId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if ("COMMAND".equalsIgnoreCase(messageCategory)) {
                switch(messageType) {
                    case "displayPatientData":
                        DisplayPatientCommand patientCmd = mapper.treeToValue(payload, DisplayPatientCommand.class);
                        log.info("Received displayPatientData: key={} value={}", correlationId, patientCmd);
                        dashboardService.handleDisplayPatientCommand(correlationId, patientCmd);
                        break;
                    case "displayInsulinDose":
                        InsulinCalculatedEvent insulinCmd = mapper.treeToValue(payload, InsulinCalculatedEvent.class);
                        log.info("Received displayInsulinDose: key={} value={}", correlationId, insulinCmd);
                        dashboardService.handleInsulinDoseCalculatedEvent(correlationId, insulinCmd);
                        break;
                    case "displayNoInsulinDose":
                        log.info("Received displayNoInsulinDose: key={}", correlationId);
                        dashboardService.handleNoInsulinDoseEventCommand(correlationId);
                        break;
                    case "displayError":
                        DisplayErrorCommand errorCmd = mapper.treeToValue(payload, DisplayErrorCommand.class);
                        log.info("Received displayError: key={} value={}", correlationId, errorCmd);
                        dashboardService.handleDisplayErrorCommand(correlationId, errorCmd);
                        break;
                    case "displayScaleReserved":
                        DisplayScaleReservedCommand scaleReservedCmd = mapper.treeToValue(payload, DisplayScaleReservedCommand.class);
                        log.info("Received displayScaleReserved: key={} value={}", correlationId, scaleReservedCmd);
                        dashboardService.handleDisplayScaleReservedCommand(correlationId);
                        break;
                    case "displayIncorrectDose":
                        DisplayIncorrectDoseCommand incorrectDoseCmd = mapper.treeToValue(payload, DisplayIncorrectDoseCommand.class);
                        log.info("Received displayIncorrectDose: key={} value={}", correlationId, incorrectDoseCmd);
                        dashboardService.handleDisplayIncorrectDoseCommand(correlationId, incorrectDoseCmd);
                        break;
                    case "displayConfirmationScreen":
                        DisplayConfirmationScreenCommand confirmScreenCmd = mapper.treeToValue(payload, DisplayConfirmationScreenCommand.class);
                        log.info("Received displayConfirmationScreen: key={} value={}", correlationId, confirmScreenCmd);
                        dashboardService.handleDisplayConfirmationScreenCommand(correlationId, confirmScreenCmd);
                        break;
                    default:
                        log.warn("Unknown messageType: {}", messageType);
                }
            }
        } catch(Exception e) {
            log.error("Error processing dashboard command", e);
        }
    }

    // Optionally, you may keep your existing insulinCalculatedRequest listener as needed.
}