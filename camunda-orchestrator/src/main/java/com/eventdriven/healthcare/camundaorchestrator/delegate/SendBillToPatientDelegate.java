// File: src/main/java/com/eventdriven/healthcare/camundaorchestrator/delegate/SendBillDelegate.java
package com.eventdriven.healthcare.camundaorchestrator.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("sendBillToPatientDelegate")
public class SendBillToPatientDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // Assume these variables are set earlier in the process
        String firstName = (String) execution.getVariable("patient_firstName");
        String lastName  = (String) execution.getVariable("patient_lastName");
        String address   = (String) execution.getVariable("patient_address");
        String city      = (String) execution.getVariable("patient_city");
        String plz       = (String) execution.getVariable("patient_plz");

        // Simple console/log output
        log.info(":::BILL::: I will send bill for patient {} {} to address: {}",
                firstName, lastName, plz + " " + city + ", " + address);
    }
}