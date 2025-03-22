package com.eventdriven.healthcare.camundaorchestrator;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableProcessApplication("application")
@EnableAspectJAutoProxy
public class CamundaOrchestratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(CamundaOrchestratorApplication.class);
  }

}