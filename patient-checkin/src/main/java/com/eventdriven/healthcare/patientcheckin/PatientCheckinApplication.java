package com.eventdriven.healthcare.patientcheckin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PatientCheckinApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientCheckinApplication.class, args);
    }

}
