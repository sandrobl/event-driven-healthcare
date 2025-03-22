package com.eventdriven.healthcare.patientdashboard.service;

import com.eventdriven.healthcare.patientdashboard.dto.DisplayErrorCommand;
import com.eventdriven.healthcare.patientdashboard.dto.InsulinCalculatedEvent;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayPatientCommand;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayIncorrectDoseCommand;
import com.eventdriven.healthcare.patientdashboard.dto.DisplayConfirmationScreenCommand;
import com.eventdriven.healthcare.patientdashboard.model.DashboardProcessInfo;
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

    private final Map<String, DashboardProcessInfo> processes = new ConcurrentHashMap<>();
    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    public void handleDisplayPatientCommand(String correlationId, DisplayPatientCommand command) {
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null, null, null, null)
        );
        Patient patient = command.getPatient();
        processInfo.setPatient(patient);
        processInfo.setCurrentStep(ProcessStep.INFORMATION_NEEDED);
        notifySseClients(processInfo);
    }

    public void handleInsulinDoseCalculatedEvent(String correlationId, InsulinCalculatedEvent command) {
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setInsulinCalculatedEvent(command);
        processInfo.setCurrentStep(ProcessStep.INSULIN_CALCULATED);
        notifySseClients(processInfo);
    }

    public void handleNoInsulinDoseEventCommand(String correlationId) {
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setCurrentStep(ProcessStep.NO_INSULIN_NEEDED);
        notifySseClients(processInfo);
    }

    public void handleDisplayErrorCommand(String correlationId, DisplayErrorCommand command) {
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setCurrentStep(ProcessStep.ERROR);
        processInfo.setErrorMessage(command.getErrorMessage());
        notifySseClients(processInfo);
    }

    public void handleDisplayScaleReservedCommand(String correlationId) {
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setCurrentStep(ProcessStep.SCALE_RESERVED);
        notifySseClients(processInfo);
    }

    public void handleDisplayIncorrectDoseCommand(String correlationId, DisplayIncorrectDoseCommand command) {
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setDoseDifference(command.getDoseDifference());
        processInfo.setCurrentStep(ProcessStep.INCORRECT_DOSE);
        notifySseClients(processInfo);
    }

    public void handleDisplayConfirmationScreenCommand(String correlationId, DisplayConfirmationScreenCommand command) {
        DashboardProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new DashboardProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setConfirmationMessage(command.getMessage());
        processInfo.setCurrentStep(ProcessStep.AWAITING_CONFIRMATION);
        notifySseClients(processInfo);
    }


    public SseEmitter createSseConnection() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            sseEmitters.remove(emitter);
        });
        sseEmitters.add(emitter);
        return emitter;
    }

    private void notifySseClients(DashboardProcessInfo processInfo) {
        for (SseEmitter emitter : sseEmitters) {
            try {
                emitter.send(SseEmitter.event().name("processUpdate").data(processInfo));
            } catch (IOException e) {
                emitter.complete();
                sseEmitters.remove(emitter);
            }
        }
    }

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