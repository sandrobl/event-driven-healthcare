package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.model.InsulinCalculationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class ProducerService<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendInsulinCalculationRequest(int patientId,
                                              int height,
                                              int weight,
                                              int bloodGlucose,
                                              float nextMealCarbohydrates,
                                              float insulinToCarbohydrateRatio,
                                              float insulinSensitivityFactor,
                                              float targetBloodGlucoseLevel) {
        logger.info("#### -> Sending insulin calculation request " +
                "request event :: " +
                "{}",patientId);

        InsulinCalculationRequest icr =
                new InsulinCalculationRequest(
                        patientId,
                        height,
                        weight,
                        bloodGlucose,
                        nextMealCarbohydrates,
                        insulinToCarbohydrateRatio,
                        insulinSensitivityFactor,
                        targetBloodGlucoseLevel);

        Message<InsulinCalculationRequest> message = MessageBuilder
                .withPayload(icr)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("type", "patientInsulinCalculatorRequest")
                .build();

        logger.info("#### -> Publishing insulin calculation request " + "{}",
                icr);

        kafkaTemplate.send(message);
    }
}