package com.eventdriven.healthcare.insulincalculator.service;

import com.eventdriven.healthcare.insulincalculator.model.InsulinCalculationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
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
            containerFactory = "kafkaListenerInsulinCalculationRequestFactory",
            groupId = "group_id")
    public void consumePatientEvent(@Payload InsulinCalculationRequest icrEvent,
                                  @Header("type") String messageType) {



        if ("insulinCalculationRequest".equals(messageType)) {
            icrEvent.setInsulinDoses(insulinCalculatorService.calculateBolusInsulinDose(icrEvent));

            producerService.sendInsulinCalculatedRequest(icrEvent);

            logger.info("**** -> Consumed patientDataRequest event :: {}",icrEvent);

        }
    }


}
