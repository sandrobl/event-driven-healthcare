package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.model.Click;
import com.eventdriven.healthcare.patientcheckin.model.Gaze;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerService<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Value("${spring.kafka.gazeEvents-topic}")
    private String gazeEventsTopic;

    @Value("${spring.kafka.clickEvents-topic}")
    private String clickEventsTopic;

    @Autowired
    private KafkaTemplate<String, Gaze> kafkaTemplateGaze;

    @Autowired
    private KafkaTemplate<String, Click> kafkaTemplateClick;


    public void startEyeTracker() {

        // Define a counter which will be used as an eventID
        int counter = 0;

        while(true) {

            // sleep for 8 ms
            try {
                Thread.sleep(8);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // generate a random gaze event using constructor  Gaze(int eventID, long timestamp, int xPosition, int yPosition, int pupilSize)
            Gaze gazeEvent = new Gaze(counter, System.nanoTime(), getRandomNumber(0, 1920), getRandomNumber(0, 1080), getRandomNumber(3, 4));
            //log
            logger.info("#### -> Publishing gaze event :: {}", gazeEvent);
            // send gaze event
            kafkaTemplateGaze.send(gazeEventsTopic, gazeEvent);

            counter++;
        }


    }


    public void startClicksTracker() {

        // Define a counter which will be used as an eventID
        int counter = 0;

        while(true) {

            // sleep for a random time interval between 500 ms and 5000 ms
            try {
                Thread.sleep(getRandomNumber(500, 5000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // generate a random click event using constructor  Clicks(int eventID, long timestamp, int xPosition, int yPosition, String clickedElement)
            Click clickEvent = new Click(counter,System.nanoTime(), getRandomNumber(0, 1920), getRandomNumber(0, 1080), "EL"+getRandomNumber(1, 20));

            // send gaze event
            kafkaTemplateClick.send(clickEventsTopic, clickEvent);

            logger.info("#### -> Publishing click event :: {}",clickEvent);

            // increment counter i.e., eventID
            counter++;

        }

    }

    /*
    Generate a random nunber
    */
    private  int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}