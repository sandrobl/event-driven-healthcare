package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.dto.InsulinCalculatedEvent;
import com.eventdriven.healthcare.patientdashboard.model.DashboardProcessInfo;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayPatientCommand;
import com.eventdriven.healthcare.patientdashboard.model.Patient;
import com.eventdriven.healthcare.patientdashboard.model.ProcessStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

@Slf4j
@Service
public class DashboardService {

    // In-memory store: correlationId -> process info
    private final Map<String, DashboardProcessInfo> processes = new ConcurrentHashMap<>();

    // Keep track of SSE emitters so we can push updates
    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    /**
     * Handle a “displayPatientData” command from Kafka.
     */
    public void handleDisplayPatientCommand(String correlationId, DisplayPatientCommand command) {
        // Get or create the process info
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null)
        );

        // Update fields based on the command
        Patient patient = command.getPatient();
        processInfo.setPatient(patient);
        processInfo.setCurrentStep(ProcessStep.INFORMATION_NEEDED);

        // Notify SSE subscribers that something changed
        notifySseClients(processInfo);
    }

    /**
     * Handle a “displayInsulinDose” command from Kafka.
     */
    public void handleInsulinDoseCalculatedEvent(String correlationId,
                                InsulinCalculatedEvent command) {

        // Get or create the process info
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null,null)
        );

        // Update fields based on the command
        processInfo.setInsulinCalculatedEvent(command);
        processInfo.setCurrentStep(ProcessStep.INSULIN_CALCULATED);
        log.info("************************Received command: {}", command);
        log.info("************************Updated process info: {}", processInfo);
        // Notify SSE subscribers that something changed
        notifySseClients(processInfo);
    }

    /**
     * Handle a “displayNoInsulinDose” command from Kafka.
     */
    public void handleNoInsulinDoseEventCommand(String correlationId){
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null)
        );
        processInfo.setCurrentStep(ProcessStep.NO_INSULIN_NEEDED);
        notifySseClients(processInfo);
    }


    /**
     * Subscribe a new SSE client.
     */
    public SseEmitter createSseConnection() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // If the connection completes or times out, remove it
        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            sseEmitters.remove(emitter);
        });

        sseEmitters.add(emitter);
        return emitter;
    }

    /**
     * Notify all SSE clients about an updated or new process.
     */
    private void notifySseClients(DashboardProcessInfo processInfo) {
        for (SseEmitter emitter : sseEmitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("processUpdate")
                                .data(processInfo)
                );
            } catch (IOException e) {
                log.error("SSE send error, removing emitter", e);
                emitter.complete();
                sseEmitters.remove(emitter);
            }
        }
    }

    /**
     * Simple read-only accessors for the UI (polling or direct GET).
     */
    public Collection<DashboardProcessInfo> getAllProcesses() {
        return processes.values();
    }

    public DashboardProcessInfo getProcess(String correlationId) {
        return processes.get(correlationId);
    }


    public void updateProcessStep(String correlationId, ProcessStep newStep) {
        DashboardProcessInfo info = processes.get(correlationId);
        if (info != null) {
            info.setCurrentStep(newStep);
            notifySseClients(info);
        }
    }
}
