package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.dto.PatientMetricsEvent;
import com.eventdriven.healthcare.patientcheckin.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Service
public class MetricsPublisherService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final PatientService patientService;
    private final KafkaTemplate<String,Object> kafka;
    private final Random rnd = new Random();

    @Value("${spring.kafka.patientStatic-topic}")
    private String staticTopic;
    @Value("${spring.kafka.patientMetrics-topic}")
    private String metricsTopic;

    public MetricsPublisherService(PatientService patientService,
                                   KafkaTemplate<String,Object> kafka) {
        this.patientService = patientService;
        this.kafka = kafka;
    }

    /**
     * On startup, publish each patient’s static info exactly once.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void publishStatic() {
        List<Patient> patients = patientService.getPatients();
        for (Patient p : patients) {
            // only nfc, name, firstname
            var payload = new PatientMetricsEvent();
            payload.setNfcId(p.getNfcID());
            payload.setName(p.getName());
            payload.setFirstname(p.getFirstname());
            // leave metrics null
            kafka.send(staticTopic, p.getNfcID(), payload);
            log.info("Published static for {}", p.getNfcID());
        }
    }

    /**
     * Every 30s, emit random height/weight updates (50% chance each).
     */
    @Scheduled(fixedRate = 30_000)
    public void emitHeightWeight() {
        patientService.getPatients().forEach(p -> {
            var m = new PatientMetricsEvent();
            m.setNfcId(p.getNfcID());
            m.setName(p.getName());
            m.setFirstname(p.getFirstname());
            if (rnd.nextBoolean()) {
                m.setHeight(140 + rnd.nextDouble() * 60);
            }
            if (rnd.nextBoolean()) {
                m.setWeight(45  + rnd.nextDouble() * 60);
            }
            m.setTimestamp(Instant.now());
            kafka.send(metricsTopic, p.getNfcID(), m);
            log.debug("HW → {} : {}", p.getNfcID(), m);
        });
    }

    /**
     * Every 10s, emit a fresh insulin‐sensitivity factor.
     */
    @Scheduled(fixedRate = 10_000)
    public void emitInsulinFactor() {
        patientService.getPatients().forEach(p -> {
            var m = new PatientMetricsEvent();
            m.setNfcId(p.getNfcID());
            m.setName(p.getName());
            m.setFirstname(p.getFirstname());
            m.setInsulinSensitivityFactor(0.5 + rnd.nextDouble() * 4.0);
            m.setTimestamp(Instant.now());
            kafka.send(metricsTopic, p.getNfcID(), m);
            log.debug("ISF → {} : {}", p.getNfcID(), m);
        });
    }
}