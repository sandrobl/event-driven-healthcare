package com.eventdriven.healthcare.patientguidancewebapp.service;

import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayErrorCommand;
import com.eventdriven.healthcare.patientguidancewebapp.dto.InsulinCalculatedEvent;
import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayPatientCommand;
import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayIncorrectDoseCommand;
import com.eventdriven.healthcare.patientguidancewebapp.dto.DisplayConfirmationScreenCommand;
import com.eventdriven.healthcare.patientguidancewebapp.model.GuidanceWebAppProcessInfo;
import com.eventdriven.healthcare.patientguidancewebapp.model.Patient;
import com.eventdriven.healthcare.patientguidancewebapp.model.ProcessStep;
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
public class GuidanceWebAppService {

    private final Map<String, GuidanceWebAppProcessInfo> processes = new ConcurrentHashMap<>();
    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    public void handleDisplayPatientCommand(String correlationId, DisplayPatientCommand command) {
        GuidanceWebAppProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new GuidanceWebAppProcessInfo(correlationId, null, null, null, null, null, null)
        );
        Patient patient = command.getPatient();
        processInfo.setPatient(patient);
        processInfo.setCurrentStep(ProcessStep.INFORMATION_NEEDED);
        notifySseClients(processInfo);
    }

    public void handleInsulinDoseCalculatedEvent(String correlationId, InsulinCalculatedEvent command) {
        GuidanceWebAppProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new GuidanceWebAppProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setInsulinCalculatedEvent(command);
        processInfo.setCurrentStep(ProcessStep.INSULIN_CALCULATED);
        notifySseClients(processInfo);
    }

    public void handleNoInsulinDoseEventCommand(String correlationId) {
        GuidanceWebAppProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new GuidanceWebAppProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setCurrentStep(ProcessStep.NO_INSULIN_NEEDED);
        notifySseClients(processInfo);
    }

    public void handleDisplayErrorCommand(String correlationId, DisplayErrorCommand command) {
        GuidanceWebAppProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new GuidanceWebAppProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setCurrentStep(ProcessStep.ERROR);
        processInfo.setErrorMessage(command.getErrorMessage());
        notifySseClients(processInfo);
    }

    public void handleDisplayScaleReservedCommand(String correlationId) {
        GuidanceWebAppProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new GuidanceWebAppProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setCurrentStep(ProcessStep.SCALE_RESERVED);
        notifySseClients(processInfo);
    }

    public void handleDisplayIncorrectDoseCommand(String correlationId, DisplayIncorrectDoseCommand command) {
        GuidanceWebAppProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new GuidanceWebAppProcessInfo(correlationId, null, null, null, null, null, null)
        );
        processInfo.setDoseDifference(command.getDoseDifference());
        processInfo.setCurrentStep(ProcessStep.INCORRECT_DOSE);
        notifySseClients(processInfo);
    }

    public void handleDisplayConfirmationScreenCommand(String correlationId, DisplayConfirmationScreenCommand command) {
        GuidanceWebAppProcessInfo processInfo = processes.computeIfAbsent(
                correlationId,
                key -> new GuidanceWebAppProcessInfo(correlationId, null, null, null, null, null, null)
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

    private void notifySseClients(GuidanceWebAppProcessInfo processInfo) {
        for (SseEmitter emitter : sseEmitters) {
            try {
                emitter.send(SseEmitter.event().name("processUpdate").data(processInfo));
            } catch (IOException e) {
                emitter.complete();
                sseEmitters.remove(emitter);
            }
        }
    }

    public Collection<GuidanceWebAppProcessInfo> getAllProcesses() {
        return processes.values();
    }

    public GuidanceWebAppProcessInfo getProcess(String correlationId) {
        return processes.get(correlationId);
    }

    public void updateProcessStep(String correlationId, ProcessStep newStep) {
        GuidanceWebAppProcessInfo info = processes.get(correlationId);
        if (info != null) {
            info.setCurrentStep(newStep);
            notifySseClients(info);
        }
    }
}