package com.eventdriven.healthcare.patientguidancewebapp.service;

import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayErrorCommand;
import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayPatientCommand;
import com.eventdriven.healthcare.patientguidancewebapp.dto.InsulinCalculatedEvent;
import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayScaleReservedCommand;
import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayIncorrectDoseCommand;
import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayConfirmationScreenCommand;
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

    private final GuidanceWebAppService guidanceWebAppService;

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
                        guidanceWebAppService.handleDisplayPatientCommand(correlationId, patientCmd);
                        break;
                    case "displayInsulinDose":
                        InsulinCalculatedEvent insulinCmd = mapper.treeToValue(payload, InsulinCalculatedEvent.class);
                        log.info("Received displayInsulinDose: key={} value={}", correlationId, insulinCmd);
                        guidanceWebAppService.handleInsulinDoseCalculatedEvent(correlationId, insulinCmd);
                        break;
                    case "displayNoInsulinDose":
                        log.info("Received displayNoInsulinDose: key={}", correlationId);
                        guidanceWebAppService.handleNoInsulinDoseEventCommand(correlationId);
                        break;
                    case "displayError":
                        DisplayErrorCommand errorCmd = mapper.treeToValue(payload, DisplayErrorCommand.class);
                        log.info("Received displayError: key={} value={}", correlationId, errorCmd);
                        guidanceWebAppService.handleDisplayErrorCommand(correlationId, errorCmd);
                        break;
                    case "displayScaleReserved":
                        DisplayScaleReservedCommand scaleReservedCmd = mapper.treeToValue(payload, DisplayScaleReservedCommand.class);
                        log.info("Received displayScaleReserved: key={} value={}", correlationId, scaleReservedCmd);
                        guidanceWebAppService.handleDisplayScaleReservedCommand(correlationId);
                        break;
                    case "displayIncorrectDose":
                        DisplayIncorrectDoseCommand incorrectDoseCmd = mapper.treeToValue(payload, DisplayIncorrectDoseCommand.class);
                        log.info("Received displayIncorrectDose: key={} value={}", correlationId, incorrectDoseCmd);
                        guidanceWebAppService.handleDisplayIncorrectDoseCommand(correlationId, incorrectDoseCmd);
                        break;
                    case "displayConfirmationScreen":
                        DisplayConfirmationScreenCommand confirmScreenCmd = mapper.treeToValue(payload, DisplayConfirmationScreenCommand.class);
                        log.info("Received displayConfirmationScreen: key={} value={}", correlationId, confirmScreenCmd);
                        guidanceWebAppService.handleDisplayConfirmationScreenCommand(correlationId, confirmScreenCmd);
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