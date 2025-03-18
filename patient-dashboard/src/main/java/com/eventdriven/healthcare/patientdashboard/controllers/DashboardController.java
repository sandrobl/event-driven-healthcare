package com.eventdriven.healthcare.patientdashboard.controllers;

import com.eventdriven.healthcare.patientdashboard.dto.InsulinFormEnteredEvent;
import com.eventdriven.healthcare.patientdashboard.model.DashboardProcessInfo;
import com.eventdriven.healthcare.patientdashboard.model.InsulinFormData;
import com.eventdriven.healthcare.patientdashboard.model.ProcessStep;
import com.eventdriven.healthcare.patientdashboard.service.DashboardService;
import com.eventdriven.healthcare.patientdashboard.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ProducerService producerService;


    /**
     * 1) SSE endpoint:
     *    The front-end can open an EventSource to /dashboard/sse
     *    and receive real-time “processUpdate” events.
     */
    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter streamSse() {
        return dashboardService.createSseConnection();
    }

    /**
     * 2) List all processes (for a simple “overview” page).
     */
    @GetMapping("/processes")
    public Collection<DashboardProcessInfo> getAllProcesses() {
        return dashboardService.getAllProcesses();
    }

    /**
     * 3) Get a single process by correlationId.
     */
    @GetMapping("/processes/{correlationId}")
    public DashboardProcessInfo getProcess(@PathVariable String correlationId) {
        return dashboardService.getProcess(correlationId);
    }

    /**
     * 4) Example: user has filled a form (like insulin dosage).
     *    Then we produce an EVENT to Kafka or just update the step in memory.
     *    In a real scenario, you’d also call dashboardService to produce a Kafka event
     *    that Camunda can consume.
     */
    @PostMapping("/processes/{correlationId}/insulinForm/submit")
    public void submitForm(
            @PathVariable String correlationId,
            @RequestBody InsulinFormData formData
    ) {
        dashboardService.updateProcessStep(correlationId, ProcessStep.FORM_SUBMITTED);

        InsulinFormEnteredEvent event = new InsulinFormEnteredEvent();

        event.setInsulinToCarbohydrateRatio(formData.getInsulinToCarbohydrateRatio());
        event.setTargetBloodGlucoseLevel(formData.getTargetBloodGlucoseLevel());
        event.setNextMealCarbohydrates(formData.getNextMealCarbohydrates());
        event.setBloodGlucose(formData.getBloodGlucose());
        event.setPatientInsulinSensitivityFactor(formData.getPatientInsulinSensitivityFactor());

        producerService.sendInsulinFormEnteredEvent(correlationId, event);
    }
}