package com.eventdriven.healthcare.camundaorchestrator.delegate;

import com.eventdriven.healthcare.camundaorchestrator.dto.domain.DisplayPatientCommand;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinCalculatedEvent;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinCalculationCommand;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

@Component("requestInsulinCalculationRESTDelegate")
@RequiredArgsConstructor
public class RequestInsulinCalculationRESTDelegate implements JavaDelegate {

    @Value("${rest.insulin-calculator-url}")
    private String insulinCalculatorUrl;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RestTemplate rest;

    @Value("${spring.kafka.patientEvents-topic}")
    private String patientEventsTopic;


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        InsulinCalculationCommand insulinCalculationCommand = new InsulinCalculationCommand();

        Float patientBloodGlucose = (Float) execution.getVariable("patient_bloodGlucose");
        Float insulinSensitivityFactor = (Float) execution.getVariable("patient_insulinSensitivityFactor");
        Float nextMealCarbohydrates = (Float) execution.getVariable("patient_nextMealCarbohydrates");
        Float insulinToCarbohydrateRatio = (Float) execution.getVariable("patient_insulinToCarbohydrateRatio");
        Float targetBloodGlucoseLevel = (Float) execution.getVariable("patient_targetBloodGlucoseLevel");
        String correlationId = execution.getProcessBusinessKey();


        insulinCalculationCommand.setInsulinToCarbohydrateRatio(insulinToCarbohydrateRatio);
        insulinCalculationCommand.setNextMealCarbohydrates(nextMealCarbohydrates);
        insulinCalculationCommand.setTargetBloodGlucoseLevel(targetBloodGlucoseLevel);

        // Call the insulin calculator API via Hystrix
        InsulinCalculatedEvent response = new HystrixCommand<InsulinCalculatedEvent>(
                HystrixCommandGroupKey.Factory.asKey("insulinCalculation")) {
            @Override
            protected InsulinCalculatedEvent run() throws Exception {

                // Call the API using the insulinCalculatorUrl variable
                return rest.postForObject(insulinCalculatorUrl, insulinCalculationCommand, InsulinCalculatedEvent.class);
            }
        }.execute();

        InsulinCalculatedEvent insulinCalculatedEvent = response;

        Message<InsulinCalculatedEvent> message = MessageBuilder.withPayload(insulinCalculatedEvent)
                .setHeader(KafkaHeaders.TOPIC, patientEventsTopic)
                .setHeader("messageCategory", "EVENT")
                .setHeader("messageType", "insulinCalculated")
                .setHeader(KafkaHeaders.KEY, correlationId)
                .build();

        kafkaTemplate.send(message);
    }
}
