package com.eventdriven.healthcare.camundaorchestrator.consumer;

import com.eventdriven.healthcare.avro.EnrichedCheckInEvent;
import com.eventdriven.healthcare.avro.MQTTScaleEvent;
import com.eventdriven.healthcare.camundaorchestrator.dto.camunda.CamundaMessageDto;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinFormEnteredEvent;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.PatientDataEvent;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.ScaleEvent;
import com.eventdriven.healthcare.avro.NfcEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private final static String MESSAGE_PATIENTDATARECIEVED = "Message_PatientDataReceived";
    private final static String MESSAGE_INSULINFORMENTERED = "Message_InsulinFormEntered";
    private final static String MESSAGE_SCALE_RESERVED = "Message_ScaleReserved";
    private final static String MESSAGE_INSULINDOSE_VALIDATED = "Message_InsulinDoseValidated";
    private final static String MESSAGE_SCALE_READING = "Message_ScaleReading";
    private final static String MESSSAGE_INJECTION_CONFIRMED = "Message_InjectionConfirmed";


    @KafkaListener(
            topics           = "${spring.kafka.nfcEvents-topic}",
            containerFactory = "kafkaListenerArvoNfcFactory",
            groupId          = "${spring.kafka.consumer.group-id}"
    )
    public void startMessageProcess(
            @Payload EnrichedCheckInEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String correlationKey
    ) {
        String nfcId   = event.getNfcID();
        String location= event.getLocation();
        int    msgId   = event.getMessageID();

        long existing = runtimeService.createProcessInstanceQuery()
                .variableValueEquals("patient_nfcId", nfcId)
                .active()
                .count();

        if (existing > 0) {
            log.info("Process already running for NFC {}. Skipping.", nfcId);
            return;
        }

        Map<String,Object> vars = new HashMap<>();
        vars.put("nfc_location",  location);
        vars.put("nfc_messageID", msgId);
        vars.put("patient_nfcId",  nfcId);
        vars.put("patient_FirstName", event.getFirstname());
        vars.put("patient_LastName", event.getName());
        vars.put("patient_height", event.getHeight());
        vars.put("patient_weight", event.getWeight());
        vars.put("patient_insulinSensitivityFactor", event.getInsulinSensitivityFactor());

        CamundaMessageDto dto = CamundaMessageDto.builder()
                .correlationId(UUID.randomUUID().toString())
                .vars(vars)
                .build();

        messageService.correlateMessage(dto, MESSAGE_NFC);
    }

    @KafkaListener(
            topics = "${spring.kafka.scaleEvents-topic}",
            containerFactory = "kafkaListenerArvoScaleFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void handleScaleEvent(@Payload MQTTScaleEvent event) {
        try {
            float weight = event.getWeight();
            logger.info("Received load_cell event with weight: {}", weight);

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
                logger.info("Correlating scale reading {} with process instance ID: {}", weight, correlationId);
                CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                        .correlationId(correlationId)
                        .vars(vars)
                        .build();
                messageService.correlateMessage(camundaMsg, MESSAGE_SCALE_READING);
            } else {
                logger.info("No process waiting for scale reading. Ignoring load_cell event.");
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
        if ("EVENT".equals(messageCategory) && "patientData".equals(messageType)) {

            try {
                ObjectMapper mapper = JsonMapper
                        .builder()
                        .addModule(new JavaTimeModule())
                        .build();
                mapper.configOverride(java.time.LocalDate.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.ARRAY));

                PatientDataEvent event = mapper.treeToValue(payload, PatientDataEvent.class);

                Map<String, Object> vars = new HashMap<>();
                vars.put("patient_found", event.isFound());
                vars.put("patient_id", event.getPatient().getPatientID());
                vars.put("patient_FirstName", event.getPatient().getFirstname());
                vars.put("patient_LastName", event.getPatient().getName());
                vars.put("patient_address", event.getPatient().getAddress());
                vars.put("patient_plz", event.getPatient().getPlz());
                vars.put("patient_city", event.getPatient().getCity());
                vars.put("patient_has_address", event.getPatient().getAddress() != null && event.getPatient().getCity() != null && event.getPatient().getPlz() != null);

                // Build a CamundaMessageDto with the correlationKey as the business key
                CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                        .correlationId(correlationKey)
                        .vars(vars)
                        .build();

                messageService.correlateMessage(camundaMsg, MESSAGE_PATIENTDATARECIEVED);
            } catch (Exception e) {
                log.error("Error deserializing payload to PatientDataEvent", e);
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

}
