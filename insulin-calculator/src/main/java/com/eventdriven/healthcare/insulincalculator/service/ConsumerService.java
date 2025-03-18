package com.eventdriven.healthcare.insulincalculator.service;

import com.eventdriven.healthcare.insulincalculator.dto.InsulinCalculatedEvent;
import com.eventdriven.healthcare.insulincalculator.dto.InsulinCalculationCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private ProducerService producerService;

    @KafkaListener(
            topics = {"${spring.kafka.patientEvents-topic}"},
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInsulinCalculationRequest(@Payload JsonNode payload,
                                                 @Header("messageCategory") String messageCategory,
                                                 @Header("messageType") String messageType,
                                                 @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Consumed {} {} {}: {}", key, messageCategory, messageType,
                payload);
        if ("COMMAND".equals(messageCategory) && "insulinFormEntered".equals(messageType)) {
            try {
                InsulinCalculationCommand command = new ObjectMapper().treeToValue(payload,
                        InsulinCalculationCommand.class);

//                logger.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, key);

                command.setInsulinDoses(insulinCalculatorService.calculateBolusInsulinDose(command));

                InsulinCalculatedEvent icrEvent = new InsulinCalculatedEvent();
                icrEvent.setInsulinRequired(command.getInsulinDoses() != 0);
                icrEvent.setInsulinDoses(command.getInsulinDoses());

                logger.info("***Sending insulin calculated event: {}, {}",
                        icrEvent.isInsulinRequired(),
                        icrEvent.getInsulinDoses()
                );

                producerService.sendInsulinCalculatedRequest(key ,icrEvent);

            } catch (Exception e) {
                logger.error("Error processing patient data request command: {}", e.getMessage());
            }
        }
    }
}
