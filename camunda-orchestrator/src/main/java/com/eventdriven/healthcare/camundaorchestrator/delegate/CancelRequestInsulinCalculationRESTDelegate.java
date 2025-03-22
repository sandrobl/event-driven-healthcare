package com.eventdriven.healthcare.camundaorchestrator.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("cancelRequestInsulinCalculationRESTDelegate")
public class CancelRequestInsulinCalculationRESTDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("--- Canceling insulin calculation request ---");
    }
}
