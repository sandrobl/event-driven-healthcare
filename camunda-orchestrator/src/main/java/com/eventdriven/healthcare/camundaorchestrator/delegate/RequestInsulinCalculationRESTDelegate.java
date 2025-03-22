package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.aop.FailingOnLastRetry;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinCalculationResult;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinCalculationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

@Slf4j
@Component("requestInsulinCalculationRESTDelegate")
@RequiredArgsConstructor
public class RequestInsulinCalculationRESTDelegate implements JavaDelegate {

    @Value("${rest.insulin-calculator-url}")
    private String insulinCalculatorUrl;

    @Autowired
    private RestTemplate rest;

    @Override
    @FailingOnLastRetry
    public void execute(DelegateExecution execution) throws Exception {
        InsulinCalculationRequest insulinCalculationRequest = new InsulinCalculationRequest();

        Float patientBloodGlucose = (Float) execution.getVariable("patient_bloodGlucose");
        Float insulinSensitivityFactor = (Float) execution.getVariable("patient_insulinSensitivityFactor");
        Float nextMealCarbohydrates = (Float) execution.getVariable("patient_nextMealCarbohydrates");
        Float insulinToCarbohydrateRatio = (Float) execution.getVariable("patient_insulinToCarbohydrateRatio");
        Float targetBloodGlucoseLevel = (Float) execution.getVariable("patient_targetBloodGlucoseLevel");

        insulinCalculationRequest.setInsulinToCarbohydrateRatio(insulinToCarbohydrateRatio);
        insulinCalculationRequest.setNextMealCarbohydrates(nextMealCarbohydrates);
        insulinCalculationRequest.setTargetBloodGlucoseLevel(targetBloodGlucoseLevel);
        insulinCalculationRequest.setBloodGlucose(patientBloodGlucose);
        insulinCalculationRequest.setPatientInsulinSensitivityFactor(insulinSensitivityFactor);

        try {
            InsulinCalculationResult response = new HystrixCommand<InsulinCalculationResult>(
                    HystrixCommandGroupKey.Factory.asKey("insulinCalculation")) {
                @Override
                protected InsulinCalculationResult run() {
                    return rest.postForObject(insulinCalculatorUrl, insulinCalculationRequest, InsulinCalculationResult.class);
                }
            }.execute();

            if (response != null) {
                execution.setVariable("insulin_doses", response.getInsulinDoses());
                execution.setVariable("insulin_required", response.isInsulinRequired());
            } else {
                log.warn("Received null response from insulin calculator");
            }

        }catch(Exception e){
            log.info("Error in calling insulin calculator API: {}",e.getMessage());
            throw e;
        }
    }
}