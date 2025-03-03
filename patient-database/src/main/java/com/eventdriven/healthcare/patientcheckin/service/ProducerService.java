package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
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



    public void startPatientsTracker() {

        // Define a counter which will be used as an eventID
        int counter = 0;

        while(true) {

            // sleep for a random time interval between 500 ms and 5000 ms
            try {
                Thread.sleep(getRandomNumber(500, 5000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Patient patientEvent = new Patient(1 + counter,"Fredson", "Fred", 182
                    ,80,
                    5.5f);

            Message<Patient> message = MessageBuilder
                    .withPayload(patientEvent)
                    .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                    .setHeader("type", "patientDataRequest")
                    .build();

            logger.info("#### -> Publishing patient event :: {}",patientEvent);

            kafkaTemplate.send(message);
            counter++;

        }

    }

    private  int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}