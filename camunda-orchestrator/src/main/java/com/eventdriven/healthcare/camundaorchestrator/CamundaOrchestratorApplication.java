package com.eventdriven.healthcare.camundaorchestrator;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication("application")
public class CamundaOrchestratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(CamundaOrchestratorApplication.class);
  }

}