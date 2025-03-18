package com.eventdriven.healthcare.insulincalculator.service;

import com.eventdriven.healthcare.insulincalculator.dto.InsulinCalculatedEvent;
import com.eventdriven.healthcare.insulincalculator.dto.InsulinCalculationCommand;
import com.eventdriven.healthcare.insulincalculator.model.InsulinCalculationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Service
public class ConsumerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InsulinCalculatorService insulinCalculatorService;

    @Autowired
    private ProducerService<InsulinCalculationRequest> producerService;

    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInsulinCalculationRequest(@Payload JsonNode payload,
                                                 @Header("messageCategory") String messageCategory,
                                                 @Header("messageType") String messageType,
                                                 @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        if ("COMMAND".equals(messageCategory) && "calculateInsulin".equals(messageType)) {
            try {
                InsulinCalculationCommand command = new ObjectMapper().treeToValue(payload,
                        InsulinCalculationCommand.class);

                logger.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, key);

                command.setInsulinDoses(insulinCalculatorService.calculateBolusInsulinDose(command));

                InsulinCalculatedEvent icrEvent = new InsulinCalculatedEvent();
                icrEvent.setInsulinRequired(command.getInsulinDoses() != 0);
                icrEvent.setPatient(command.getPatient());
                icrEvent.setInsulinDoses(command.getInsulinDoses());

                producerService.sendInsulinCalculatedRequest(icrEvent);

            } catch (Exception e) {
                logger.error("Error processing patient data request command: {}", e.getMessage());
            }
        }
    }
}
