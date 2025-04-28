package com.eventdriven.healthcare.scale.service;

import com.eventdriven.healthcare.scale.dto.ScaleCommand;
import com.eventdriven.healthcare.scale.model.ScaleReservationRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class ScaleService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Inject the ProducerService to publish events back to Kafka.
    private final ProducerService producerService;

    // Queue to hold waiting scale reservation requests.
    private final Queue<ScaleReservationRequest> waitingQueue = new LinkedList<>();

    // The current active reservation (if any).
    private ScaleReservationRequest currentReservation = null;

    /**
     * Reserve the scale.
     * If free, reserve immediately and publish a "scaleReserved" event.
     * Otherwise, queue the request.
     */
    public void reserveScale(ScaleCommand cmd) {
        logger.info("Reserve scale for correlationId={}", cmd.getCorrelationId());
        if (currentReservation == null) {
            currentReservation = new ScaleReservationRequest(cmd.getCorrelationId());
            logger.info("Scale reserved for correlationId={}", cmd.getCorrelationId());
            producerService.publishScaleReserved(cmd.getCorrelationId());
        } else {
            waitingQueue.add(new ScaleReservationRequest(cmd.getCorrelationId()));
            logger.info("Scale busy. correlationId={} added to queue. Queue size={}",
                    cmd.getCorrelationId(), waitingQueue.size());
        }
    }

    /**
     * Unreserve the scale if the correlationId matches the current reservation.
     * Then check the waiting queue to reserve the next request.
     */
    public void unreserveScale(ScaleCommand cmd) {
        logger.info("Unreserve scale for correlationId={}", cmd.getCorrelationId());
        if (currentReservation != null
                && currentReservation.getCorrelationId().equals(cmd.getCorrelationId())) {
            currentReservation = null;
            producerService.publishScaleUnreserved(cmd.getCorrelationId());
            logger.info("Scale unreserved for correlationId={}", cmd.getCorrelationId());
            processNextReservation();
        } else {
            logger.info("Ignoring unreserveScale. Either no current reservation or correlationId mismatch.");
        }
    }

    /**
     * Validate the insulin dose by updating the current reservation with the dose.
     * Actual dose comparison will occur when the next "load_cell" event is processed.
     */
    public void validateDose(ScaleCommand cmd) {
        logger.info("Validate dose for correlationId={}, insulinDose={}",
                cmd.getCorrelationId(), cmd.getInsulinDose());
        if (currentReservation == null) {
            logger.warn("No current reservation, cannot validate dose.");
            return;
        }
        if (!currentReservation.getCorrelationId().equals(cmd.getCorrelationId())) {
            logger.warn("validateInsulinDose correlationId mismatch. Current reservation is {}, ignoring.",
                    currentReservation.getCorrelationId());
            return;
        }
        Float requestedDose = cmd.getInsulinDose();
        Float scaleValue = cmd.getScaleValue();

        float doseValue = (requestedDose < 1) ? 1 : Math.round(requestedDose);
        float doseDifference = scaleValue - doseValue;

        if (doseDifference == 0f) {
            producerService.publishInsulinDoseValidated(currentReservation.getCorrelationId(),true, 0.0f);
            logger.info("Insulin dose valid for correlationId={}", currentReservation.getCorrelationId());
        } else {
            producerService.publishInsulinDoseValidated(currentReservation.getCorrelationId(),false, doseDifference);
            logger.info("Insulin dose invalid for correlationId={}", currentReservation.getCorrelationId());
        }

    }

    /**
     * Process the next waiting reservation if available.
     */
    private void processNextReservation() {
        if (currentReservation == null && !waitingQueue.isEmpty()) {
            currentReservation = waitingQueue.poll();
            logger.info("Scale now reserved from queue for correlationId={}",
                    currentReservation.getCorrelationId());
            producerService.publishScaleReserved(currentReservation.getCorrelationId());
        }
    }
}