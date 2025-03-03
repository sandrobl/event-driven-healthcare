package com.eventdriven.healthcare.patientcheckin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class PatientDatabaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientDatabaseApplication.class, args);
    }

}
