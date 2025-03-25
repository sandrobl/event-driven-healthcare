package com.eventdriven.healthcare.patientguidancewebapp.controllers;

import com.eventdriven.healthcare.patientguidancewebapp.dto.InjectionConfirmedEvent;
import com.eventdriven.healthcare.patientguidancewebapp.dto.InsulinFormEnteredEvent;
import com.eventdriven.healthcare.patientguidancewebapp.model.GuidanceWebAppProcessInfo;
import com.eventdriven.healthcare.patientguidancewebapp.model.InsulinFormData;
import com.eventdriven.healthcare.patientguidancewebapp.model.ProcessStep;
import com.eventdriven.healthcare.patientguidancewebapp.service.GuidanceWebAppService;
import com.eventdriven.healthcare.patientguidancewebapp.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class GuidanceWebAppController {

    private final GuidanceWebAppService guidanceWebAppService;
    private final ProducerService producerService;


    /**
     * 1) SSE endpoint:
     *    The front-end can open an EventSource to /dashboard/sse
     *    and receive real-time “processUpdate” events.
     */
    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter streamSse() {
        return guidanceWebAppService.createSseConnection();
    }

    /**
     * 2) List all processes (for a simple “overview” page).
     */
    @GetMapping("/processes")
    public Collection<GuidanceWebAppProcessInfo> getAllProcesses() {
        return guidanceWebAppService.getAllProcesses();
    }

    /**
     * 3) Get a single process by correlationId.
     */
    @GetMapping("/processes/{correlationId}")
    public GuidanceWebAppProcessInfo getProcess(@PathVariable("correlationId") String correlationId) {
        return guidanceWebAppService.getProcess(correlationId);
    }

    /**
     * 4) Example: user has filled a form (like insulin dosage).
     *    Then we produce an EVENT to Kafka or just update the step in memory.
     *    In a real scenario, you’d also call dashboardService to produce a Kafka event
     *    that Camunda can consume.
     */
    @PostMapping("/processes/{correlationId}/insulinForm/submit")
    public void submitForm(
            @PathVariable("correlationId") String correlationId,
            @RequestBody InsulinFormData formData
    ) {
        guidanceWebAppService.updateProcessStep(correlationId, ProcessStep.FORM_SUBMITTED);

        InsulinFormEnteredEvent event = new InsulinFormEnteredEvent();

        event.setInsulinToCarbohydrateRatio(formData.getInsulinToCarbohydrateRatio());
        event.setTargetBloodGlucoseLevel(formData.getTargetBloodGlucoseLevel());
        event.setNextMealCarbohydrates(formData.getNextMealCarbohydrates());
        event.setBloodGlucose(formData.getBloodGlucose());
        event.setPatientInsulinSensitivityFactor(formData.getPatientInsulinSensitivityFactor());

        producerService.sendInsulinFormEnteredEvent(correlationId, event);
    }

    @PostMapping("/processes/{correlationId}/confirmInjection")
    public void confirmInjection(@PathVariable("correlationId") String correlationId,
            @RequestBody InjectionConfirmedEvent event) {
        guidanceWebAppService.updateProcessStep(correlationId, ProcessStep.CONFIRMED);
        producerService.sendInjectionConfirmedEvent(correlationId, event);
    }

}