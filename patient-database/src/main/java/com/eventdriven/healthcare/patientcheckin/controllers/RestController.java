package com.eventdriven.healthcare.patientcheckin.controllers;

import com.eventdriven.healthcare.patientcheckin.model.Patient;
import com.eventdriven.healthcare.patientcheckin.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.eventdriven.healthcare.patientcheckin.service.ProducerService;

import java.util.List;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(value = "/kafka")
public class RestController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProducerService<Patient> producerService;

    @Autowired
    private final PatientService patientService;

    public RestController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping(value = "/getAllPatients")
    public List<Patient> eyeTrackingCall() {
        logger.info("Getting all patients");

        return patientService.getPatients();
    }
}