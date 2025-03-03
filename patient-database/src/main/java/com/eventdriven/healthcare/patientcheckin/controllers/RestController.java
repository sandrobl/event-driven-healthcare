package com.eventdriven.healthcare.patientcheckin.controllers;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
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

    @Autowired
    private ProducerService<Patient> producerService;

    @GetMapping(value = "/patientTracking")
    public String eyeTrackingCall(@RequestParam("action") String action) {
        String output = "";

        if(action.equals("start") & !singleEyeTrackingThreadStart) {
            // starting eye-tracking in a new thread if not already started
            Thread eyeTrackingThread =  new Thread(() -> {
                producerService.startPatientsTracker();
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
}