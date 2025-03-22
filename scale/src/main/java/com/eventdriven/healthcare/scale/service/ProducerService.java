package com.eventdriven.healthcare.scale.service;

import com.eventdriven.healthcare.scale.dto.ScaleEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProducerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    public void publishScaleReserved(String correlationId) {
        ScaleEvent event = new ScaleEvent();
        event.setType(ScaleEvent.Type.SCALE_RESERVED);
        // Other fields remain null for this event.
        Message<ScaleEvent> msg = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "scaleReserved")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();
        kafkaTemplate.send(msg);
        logger.info("Published scaleReserved for correlationId={}", correlationId);
    }

    public void publishScaleUnreserved(String correlationId) {
        ScaleEvent event = new ScaleEvent();
        event.setType(ScaleEvent.Type.SCALE_UNRESERVED);
        Message<ScaleEvent> msg = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "scaleUnreserved")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();
        kafkaTemplate.send(msg);
        logger.info("Published scaleUnreserved for correlationId={}", correlationId);
    }

    /**
     * Publishes a single insulin dose validated event.
     *
     * @param correlationId the process correlation identifier
     * @param valid         true if the measured dose is valid, false otherwise
     * @param doseDifference the difference between the measured and requested dose
     */
    public void publishInsulinDoseValidated(String correlationId, boolean valid, float doseDifference) {
        ScaleEvent event = new ScaleEvent();
        event.setType(ScaleEvent.Type.INSULIN_DOSE_VALIDATED);
        event.setInsulinDoseValid(valid);
        event.setDoseDifference(doseDifference);
        Message<ScaleEvent> msg = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "insulinDoseValidated")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();
        kafkaTemplate.send(msg);
        logger.info("Published insulinDoseValidated for correlationId={}, valid={}, doseDifference={}",
                correlationId, valid, doseDifference);
    }
}