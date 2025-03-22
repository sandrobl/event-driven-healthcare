package com.eventdriven.healthcare.scale.service;

import com.eventdriven.healthcare.scale.dto.ScaleCommand;
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

    // Delegate all business logic to ScaleService.
    private final ScaleService scaleService;

    /**
     * Listen for COMMAND messages (reserve, unreserve, validate dose)
     * coming from the patientEvents-topic.
     */
    @KafkaListener(
            topics = "${spring.kafka.patientEvents-topic}",
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeScaleCommands(
            @Payload JsonNode payload,
            @Header("messageCategory") String messageCategory,
            @Header("messageType") String messageType,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        // Process only scale COMMAND messages.
        if (!"COMMAND".equalsIgnoreCase(messageCategory) ) {
            return;
        }
        if (!( "reserveScale".equalsIgnoreCase(messageType)
                || "unreserveScale".equalsIgnoreCase(messageType)
                || "validateInsulinDose".equalsIgnoreCase(messageType))) {
            // Ignore messages that are not one of the specified types.
            return;
        }

        try {
            ScaleCommand cmd = new ObjectMapper().treeToValue(payload, ScaleCommand.class);
            switch (cmd.getAction()) {
                case RESERVE_SCALE:
                    scaleService.reserveScale(cmd);
                    break;
                case UNRESERVE_SCALE:
                    scaleService.unreserveScale(cmd);
                    break;
                case VALIDATE_DOSE:
                    scaleService.validateDose(cmd);
                    break;
                default:
                    logger.warn("Unknown scale command action: {}", cmd.getAction());
            }
        } catch (Exception e) {
            logger.error("Error parsing ScaleCommand: {}", e.getMessage());
        }
    }
}