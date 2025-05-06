package com.eventdriven.healthcare.patientcheckin.service;

import com.eventdriven.healthcare.patientcheckin.dto.PatientMetricsEvent;
import com.eventdriven.healthcare.patientcheckin.dto.PatientStaticEvent;
import com.eventdriven.healthcare.patientcheckin.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetricsPublisherService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final PatientService patientService;
    private final KafkaTemplate<String,Object> kafka;
    private final Random rnd = new Random();

    // in-memory cache of the last full metrics snapshot per NFC ID
    private final Map<String, PatientMetricsEvent> latestMetrics = new ConcurrentHashMap<>();

    @Value("${spring.kafka.patientStatic-topic}")
    private String staticTopic;
    @Value("${spring.kafka.patientMetrics-topic}")
    private String metricsTopic;

    public MetricsPublisherService(PatientService patientService,
                                   KafkaTemplate<String,Object> kafka) {
        this.patientService = patientService;
        this.kafka = kafka;
    }

    /** On startup, publish static info + initial full snapshot */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        List<Patient> patients = patientService.getPatients();
        for (Patient p : patients) {
            String key = p.getNfcID();
            // 1) static
            PatientStaticEvent ps = PatientStaticEvent.builder()
                    .nfcId(key)
                    .name(p.getName())
                    .firstname(p.getFirstname())
                    .build();
            kafka.send(staticTopic, key, ps);

            // 2) initial metrics: random but complete
            double h   = 150 + rnd.nextDouble() * 30;    // 150–180cm
            double w   = 50  + rnd.nextDouble() * 40;    // 50–90kg
            double isf = 0.5 + rnd.nextDouble() * 4.0;   // 0.5–4.5

            PatientMetricsEvent pm = PatientMetricsEvent.builder()
                    .nfcId(key)
                    .height(h)
                    .weight(w)
                    .insulinSensitivityFactor(isf)
                    .timestamp(Instant.now())
                    .build();

            latestMetrics.put(key, pm);
            kafka.send(metricsTopic, key, pm);
            log.info("Init metrics {} → {}", key, pm);
        }
    }

    /** Every 30s: adjust weight by ±0.5 kg, carry height+ISF */
    @Scheduled(fixedRate = 30_000)
    public void emitWeightUpdate() {
        for (Patient p : patientService.getPatients()) {
            String key = p.getNfcID();
            PatientMetricsEvent prev = latestMetrics.get(key);
            if (prev == null) continue;

            double delta = (rnd.nextDouble() * 2 - 1) * 0.5;  // [-0.5, +0.5]
            double newW  = prev.getWeight() + delta;

            PatientMetricsEvent next = PatientMetricsEvent.builder()
                    .nfcId(key)
                    .height(prev.getHeight())
                    .weight(newW)
                    .insulinSensitivityFactor(prev.getInsulinSensitivityFactor())
                    .timestamp(Instant.now())
                    .build();

            latestMetrics.put(key, next);
            kafka.send(metricsTopic, key, next);
            log.debug("Weight update {} → {}", key, next);
        }
    }

    /** Every hour: 1% chance tweak height by ±0.1 cm */
    @Scheduled(fixedRate = 3_600_000)
    public void emitHeightUpdate() {
        for (Patient p : patientService.getPatients()) {
            String key = p.getNfcID();
            PatientMetricsEvent prev = latestMetrics.get(key);
            if (prev == null) continue;

            if (rnd.nextDouble() < 0.01) { // only 1% of runs
                double delta = (rnd.nextDouble() * 2 - 1) * 0.1; // [-0.1, +0.1]
                double newH  = prev.getHeight() + delta;

                PatientMetricsEvent next = PatientMetricsEvent.builder()
                        .nfcId(key)
                        .height(newH)
                        .weight(prev.getWeight())
                        .insulinSensitivityFactor(prev.getInsulinSensitivityFactor())
                        .timestamp(Instant.now())
                        .build();

                latestMetrics.put(key, next);
                kafka.send(metricsTopic, key, next);
                log.info("Height tweak   {} → {}", key, next);
            }
        }
    }

    /** Every 10s: new ISF, carry height+weight */
    @Scheduled(fixedRate = 10_000)
    public void emitInsulinFactorUpdate() {
        for (Patient p : patientService.getPatients()) {
            String key = p.getNfcID();
            PatientMetricsEvent prev = latestMetrics.get(key);
            if (prev == null) continue;

            double drift   = (rnd.nextDouble() * 2 - 1) * 0.02;
            double newIsf  = prev.getInsulinSensitivityFactor() * (1 + drift);

            PatientMetricsEvent next = PatientMetricsEvent.builder()
                    .nfcId(key)
                    .height(prev.getHeight())
                    .weight(prev.getWeight())
                    .insulinSensitivityFactor(newIsf)
                    .timestamp(Instant.now())
                    .build();

            latestMetrics.put(key, next);
            kafka.send(metricsTopic, key, next);
            log.debug("ISF update     {} → {}", key, next);
        }
    }
}