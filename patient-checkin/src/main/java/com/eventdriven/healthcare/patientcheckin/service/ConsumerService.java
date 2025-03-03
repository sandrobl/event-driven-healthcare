package com.eventdriven.healthcare.patientcheckin.service;


import com.eventdriven.healthcare.patientcheckin.model.Click;
import com.eventdriven.healthcare.patientcheckin.model.Gaze;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
            topics = {"${spring.kafka.gazeEvents-topic}"},
            containerFactory = "kafkaListenerGazeFactory",
            groupId = "group_id")
    public void consumeGazeEvent(@Payload Gaze gazeEvent,
                                 @Header("type") String messageType) {
        if ("test".equals(messageType)) {
            logger.info("**** -> Consumed test gaze event :: {}",
                    gazeEvent);
            return;
        }else{
            logger.info("**** -> Consumed gaze event :: {}", gazeEvent);
        }

        // consumed gaze events can be processed here ....

    }

    @KafkaListener(
            topics = {"${spring.kafka.clickEvents-topic}"},
            containerFactory = "kafkaListenerClickFactory",
            groupId = "group_id")
    public void consumeClickEvent(Click clickEvent) {

        logger.info("**** -> Consumed click event :: {}", clickEvent);

        // consumed click events can be processed here ....

    }

}
