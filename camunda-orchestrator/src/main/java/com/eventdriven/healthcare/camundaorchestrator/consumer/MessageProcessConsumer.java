package com.eventdriven.healthcare.camundaorchestrator.consumer;

import com.eventdriven.healthcare.camundaorchestrator.dto.camunda.CamundaMessageDto;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinFormEnteredEvent;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.PatientCheckInEvent;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.ScaleEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageProcessConsumer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageService messageService;
    private final RuntimeService runtimeService;

    private final static String MESSAGE_NFC = "Message_NFCTag";
    private final static String MESSAGE_PATIENTCHECKEDIN = "Message_PatientCheckedIn";
    private final static String MESSAGE_INSULINFORMENTERED = "Message_InsulinFormEntered";
    private final static String MESSAGE_SCALE_RESERVED = "Message_ScaleReserved";
    private final static String MESSAGE_INSULINDOSE_VALIDATED = "Message_InsulinDoseValidated";
    private final static String MESSAGE_SCALE_READING = "Message_ScaleReading";
    private final static String MESSSAGE_INJECTION_CONFIRMED = "Message_InjectionConfirmed";

    @KafkaListener(
            topics = "${spring.kafka.mqttEvents-topic}",
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void startMessageProcess(@Payload JsonNode healthCareEvent) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(healthCareEvent.toString());
            String type = rootNode.get("type").asText();

            if ("nfc".equalsIgnoreCase(type)) {
                String location = rootNode.get("location").asText();
                String messageID = rootNode.get("messageID").asText();
                int readingId = Integer.parseInt(rootNode.get("readingID").asText());
                if (readingId == 0) {
                    return;
                }
                String formattedNfcID = formatNfcId(rootNode.get("ID").asText());

                long existingCount = runtimeService.createProcessInstanceQuery()
                        .variableValueEquals("patient_nfcId", formattedNfcID)
                        .active()
                        .count();

                if (existingCount > 0) {
                    logger.info("Process already running with NFC ID {}. Skip start process.", formattedNfcID);
                    return;
                }

                Map<String, Object> vars = new HashMap<>();
                vars.put("nfc_location", location);
                vars.put("nfc_messageID", messageID);
                vars.put("patient_nfcId", formattedNfcID);

                // Generate a unique correlation ID.
                String correlationId = UUID.randomUUID().toString();

                CamundaMessageDto camundaMessageDto = CamundaMessageDto.builder()
                        .correlationId(correlationId)
                        .vars(vars)
                        .build();

                messageService.correlateMessage(camundaMessageDto, MESSAGE_NFC);
            } else if ("load_cell".equalsIgnoreCase(type)) {
                float weight = rootNode.get("weight").floatValue();
                logger.info("Received load_cell event with weight: {}", weight);

                if (weight < 1f) {
                    logger.info("Ignoring load_cell event with weight < 1.");
                    return;
                }

                // Query Camunda for an execution waiting for the "Message_ScaleReading" event.
                Execution waitingExecution = runtimeService.createExecutionQuery()
                        .messageEventSubscriptionName(MESSAGE_SCALE_READING)
                        .singleResult();
                if (waitingExecution != null) {
                    // Use the business key if available; otherwise, use the process instance ID.
                    String processInstanceId = waitingExecution.getProcessInstanceId();
                    String correlationId = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(processInstanceId)
                            .singleResult()
                            .getBusinessKey();
                    if (correlationId == null) {
                        correlationId = processInstanceId;
                    }

                    Map<String, Object> vars = new HashMap<>();
                    vars.put("latest_scale_value", weight);

                    CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                            .correlationId(correlationId)
                            .vars(vars)
                            .build();
                    messageService.correlateMessage(camundaMsg, MESSAGE_SCALE_READING);
                } else {
                    logger.info("No process waiting for scale reading. Ignoring load_cell event.");
                }
            }
        } catch (Exception e) {
            logger.error("Error processing NFC/scale hardware event: ", e);
        }
    }

    @KafkaListener(
            topics = "${spring.kafka.patientEvents-topic}",
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEvents(@Payload JsonNode payload,
                              @Header("messageCategory") String messageCategory,
                              @Header("messageType") String messageType,
                              @Header(KafkaHeaders.RECEIVED_KEY) String correlationKey) {
        log.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, correlationKey);
        if ("EVENT".equals(messageCategory) && "patientCheckedIn".equals(messageType)) {

            try {
                PatientCheckInEvent event = new ObjectMapper().treeToValue(payload, PatientCheckInEvent.class);

                Map<String, Object> vars = new HashMap<>();
                vars.put("patient_found", event.isFound());
                vars.put("patient_id", event.getPatient().getPatientID());
                vars.put("patient_FirstName", event.getPatient().getFirstname());
                vars.put("patient_LastName", event.getPatient().getName());
                vars.put("patient_height", event.getPatient().getHeight());
                vars.put("patient_weight", event.getPatient().getWeight());
                vars.put("patient_insulinSensitivityFactor", event.getPatient().getInsulinSensitivityFactor());

                // Build a CamundaMessageDto with the correlationKey as the business key
                CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                        .correlationId(correlationKey)
                        .vars(vars)
                        .build();

                messageService.correlateMessage(camundaMsg, MESSAGE_PATIENTCHECKEDIN);
            } catch (Exception e) {
                log.error("Error deserializing payload to PatientCheckInEvent", e);
            }
        } else if ("EVENT".equals(messageCategory) && "insulinFormEntered".equals(messageType)) {
            try {
                InsulinFormEnteredEvent event =
                        new ObjectMapper().treeToValue(payload, InsulinFormEnteredEvent.class);

                Map<String, Object> vars = new HashMap<>();
                vars.put("patient_nextMealCarbohydrates", event.getNextMealCarbohydrates());
                vars.put("patient_insulinToCarbohydrateRatio", event.getInsulinToCarbohydrateRatio());
                vars.put("patient_targetBloodGlucoseLevel", event.getTargetBloodGlucoseLevel());
                vars.put("patient_bloodGlucose", event.getBloodGlucose());
                vars.put("patient_insulinSensitivityFactor",
                        event.getPatientInsulinSensitivityFactor());

                // Build a CamundaMessageDto with the correlationKey as the business key
                CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                        .correlationId(correlationKey)
                        .vars(vars)
                        .build();
                messageService.correlateMessage(camundaMsg, MESSAGE_INSULINFORMENTERED);

            } catch (Exception e) {
                log.error("Error deserializing payload to PatientCheckInEvent", e);
            }
        } else if ("EVENT".equals(messageCategory) && "scaleReserved".equalsIgnoreCase(messageType)) {
            // Directly correlate the scaleReserved event.
            CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                    .correlationId(correlationKey)
                    .build();
            messageService.correlateMessage(camundaMsg, MESSAGE_SCALE_RESERVED);
        } else if ("EVENT".equals(messageCategory) && "insulinDoseValidated".equalsIgnoreCase(messageType)) {
            try {

                ScaleEvent scaleEvent = new ObjectMapper().treeToValue(payload, ScaleEvent.class);
                Map<String, Object> vars = new HashMap<>();
                vars.put("scale_dose_valid", scaleEvent.getInsulinDoseValid());
                vars.put("scale_validation_dose_difference", scaleEvent.getDoseDifference());
                CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                        .correlationId(correlationKey)
                        .vars(vars)
                        .build();
                messageService.correlateMessage(camundaMsg, MESSAGE_INSULINDOSE_VALIDATED);
            } catch (Exception e) {
                log.error("Error deserializing payload to InsulinDoseValidated", e);
            }
        } else if ("EVENT".equals(messageCategory) && "injectionConfirmed".equalsIgnoreCase(messageType)) {
            // Directly correlate the scaleReserved event.
            CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                    .correlationId(correlationKey)
                    .build();
            messageService.correlateMessage(camundaMsg, MESSSAGE_INJECTION_CONFIRMED);
        }
    }


    /**
     * Converts an NFC ID from event format (e.g., "ID 0x04 0xDA 0xF2 ...")
     * into a format that matches the database (e.g., "04DAF28AB45780").
     */
    private String formatNfcId(String rawNfcId) {
        if (rawNfcId == null || !rawNfcId.startsWith("ID ")) {
            return null; // Invalid format
        }
        return rawNfcId.replace("ID ", "") // Remove "ID " prefix
                .replaceAll("0x", "") // Remove "0x" prefixes
                .replaceAll(" ", ""); // Remove spaces
    }

}
