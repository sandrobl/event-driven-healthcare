package com.eventdriven.healthcare.insulincalculator.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.eventdriven.healthcare.insulincalculator.service.ProducerService;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(value = "/kafka")
public class RestController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

}