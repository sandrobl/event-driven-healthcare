package com.eventdriven.healthcare.patientcheckin.controllers;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.eventdriven.healthcare.patientcheckin.service.ProducerService;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(value = "/kafka")
public class RestController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProducerService<Patient> producerService;

    @GetMapping(value = "/patientInformationRequest")
    public String patientInformationRequest(@RequestParam("patientId") int patientId) {
        logger.info("Patient information request for patient ID :: " + patientId);
        if(patientId > 0) {
            producerService.sendPatientInformationRequest(patientId);
            return "Patient information request sent for patient ID :: " + patientId;
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid patient ID");
        }
    }
}