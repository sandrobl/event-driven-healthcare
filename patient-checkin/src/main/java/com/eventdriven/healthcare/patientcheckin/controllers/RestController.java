package com.eventdriven.healthcare.patientcheckin.controllers;

import com.eventdriven.healthcare.patientcheckin.model.Gaze;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.eventdriven.healthcare.patientcheckin.service.ProducerService;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(value = "/kafka")
public class RestController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    Boolean singleEyeTrackingThreadStart = false;
    Boolean singleClickTrackingThreadStart = false;

    @Autowired
    private ProducerService<Gaze> producerService;

    @GetMapping(value = "/eyeTracking")
    public String eyeTrackingCall(@RequestParam("action") String action) {
        String output = "";

        if(action.equals("start") & !singleEyeTrackingThreadStart) {
            // starting eye-tracking in a new thread if not already started
            Thread eyeTrackingThread =  new Thread(() -> {
                producerService.startEyeTracker();
            });
            eyeTrackingThread.start();
            singleEyeTrackingThreadStart = true;
            output = "Eye-tracker successfully started!";
        }
        else if(singleEyeTrackingThreadStart) {
            output = "Eye-tracker already running";
        }
        else {
            output = "Unknown action";
        }

        return output;
    }

    @GetMapping(value = "/eyeTrackingStop")
    public String eyeTrackingStop() {
        logger.info("Stopping eye tracking");
        return "Eye tracking stopped";
    }

    @GetMapping(value = "/clickTracking")
    public String clickTrackingCall(@RequestParam("action") String action) {
        String output = "";

        logger.info("action: " + action);

        if(action.equals("start") & !singleClickTrackingThreadStart) {
            // starting clicks tracking in a new thread if not already started
            Thread clickTrackingThread =  new Thread(() -> {
                producerService.startClicksTracker();
            });
            clickTrackingThread.start();
            singleClickTrackingThreadStart = true;
            output = "Started recording user clicks!";
        }
        else if(singleClickTrackingThreadStart) {
            output = "the recording of users clicks is already running";
        }
        else {
            output = "Unknown action";
        }

        return output;
    }






}