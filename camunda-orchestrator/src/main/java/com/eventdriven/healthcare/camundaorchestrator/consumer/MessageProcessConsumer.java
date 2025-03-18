package com.eventdriven.healthcare.camundaorchestrator.consumer;

import com.eventdriven.healthcare.camundaorchestrator.dto.camunda.CamundaMessageDto;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.InsulinCalculatedEvent;
import com.eventdriven.healthcare.camundaorchestrator.dto.domain.PatientCheckInEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
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
    private final static String MESSAGE_INSULINCALCULATED =
            "Message_InsulinCalculated";


    @KafkaListener(
            topics = "${spring.kafka.mqttEvents-topic}",
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void startMessageProcess(@Payload JsonNode healthCareEvent){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(healthCareEvent.toString());
            String type = rootNode.get("type").asText();

            if("nfc".equalsIgnoreCase(type)){
                String location = rootNode.get("location").asText();
                String messageID = rootNode.get("messageID").asText();
                int readingId =
                        Integer.parseInt(rootNode.get("readingID").asText());

                if(readingId == 0){
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

                // Build process variables map
                Map<String, Object> vars = new HashMap<>();
                vars.put("nfc_location", location);
                vars.put("nfc_messageID", messageID);
                vars.put("patient_nfcId", formattedNfcID);

                // Generate a unique correlation ID, e.g., based on readingId or formattedNfcID
                String correlationId = UUID.randomUUID().toString();

                // Create CamundaMessageDto with the correlation id and variables
                CamundaMessageDto camundaMessageDto = CamundaMessageDto.builder()
                        .correlationId(correlationId)
                        .vars(vars)
                        .build();

                // Correlate message using the service. Ensure MESSAGE_START equals "Message_NFCTag" (BPMN start event name)
                messageService.correlateMessage(camundaMessageDto, MESSAGE_NFC);
            }
        } catch (Exception e) {
            logger.error("Error processing NFC event: ", e);
        }
    }

    @KafkaListener(
            topics = "${spring.kafka.patientEvents-topic}",
            containerFactory = "kafkaListenerJsonFactory",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumePatientCheckInEvent(@Payload JsonNode payload,
                                           @Header("messageCategory") String messageCategory,
                                           @Header("messageType") String messageType,
                                           @Header(KafkaHeaders.RECEIVED_KEY) String correlationKey) {
        if ("EVENT".equals(messageCategory) && "patientCheckedIn".equals(messageType)) {
            log.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, correlationKey);

            try {
                PatientCheckInEvent event = new ObjectMapper().treeToValue(payload, PatientCheckInEvent.class);

                Map<String, Object> vars = new HashMap<>();
                vars.put("patient_found", event.isFound());
                vars.put("patient_id", event.getPatient().getPatientID());
                vars.put("patient_FirstName", event.getPatient().getFirstname());
                vars.put("patient_LastName", event.getPatient().getName());
                vars.put("patient_height", event.getPatient().getHeight());
                vars.put("patient_weight", event.getPatient().getWeight());
                vars.put("patient_bloodGlucose", event.getPatient().getBloodGlucose());

                // Build a CamundaMessageDto with the correlationKey as the business key
                CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                        .correlationId(correlationKey)
                        .vars(vars)
                        .build();

                messageService.correlateMessage(camundaMsg, MESSAGE_PATIENTCHECKEDIN);
            } catch(Exception e){
                log.error("Error deserializing payload to PatientCheckInEvent", e);
            }
        } else if ("EVENT".equals(messageCategory) && "insulinCalculated".equals(messageType)) {
            log.info("Consumed {} {}: {} with key: {}", messageCategory, messageType, payload, correlationKey);
            try {
                InsulinCalculatedEvent event =
                        new ObjectMapper().treeToValue(payload, InsulinCalculatedEvent.class);

                Map<String, Object> vars = new HashMap<>();
                vars.put("patient_id", event.getPatient().getPatientID());
                vars.put("insulin_doses", event.getInsulinDoses());
                vars.put("insulin_required", event.isInsulinRequired());

                // Build a CamundaMessageDto with the correlationKey as the business key
                CamundaMessageDto camundaMsg = CamundaMessageDto.builder()
                        .correlationId(correlationKey)
                        .vars(vars)
                        .build();
                messageService.correlateMessage(camundaMsg, MESSAGE_INSULINCALCULATED);

            } catch(Exception e){
                log.error("Error deserializing payload to PatientCheckInEvent", e);
            }
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
