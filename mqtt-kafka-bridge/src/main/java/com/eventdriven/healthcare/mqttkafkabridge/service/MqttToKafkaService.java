package com.eventdriven.healthcare.mqttkafkabridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class MqttToKafkaService implements MqttCallback {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${mqtt.topic}")
    private String mqttTopic;

    @Value("${kafka.topic}")
    private String kafkaTopic;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws Exception {
        logger.info("Initializing MQTT subscription to topic: {}", mqttTopic);
        mqttClient.setCallback(this);
        mqttClient.subscribe(mqttTopic);
    }

    @Override
    public void connectionLost(Throwable cause) {
        // Handle lost connection
        logger.error("MQTT connection lost: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        logger.info("Received MQTT message on topic {}: {}", topic, payload);

        String key;
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(payload);
            // Prefer UID for grouping; fallback to messageID if UID is not present
            if (jsonNode.has("UID")) {
                key = jsonNode.get("UID").asText();
            } else if (jsonNode.has("messageID")) {
                key = jsonNode.get("messageID").asText();
            } else {
                key = "unknown";
            }
        } catch (Exception e) {
            logger.error("Invalid JSON payload received: {}", payload, e);
            return;
        }

        kafkaTemplate.send(kafkaTopic, key, jsonNode);
        logger.info("Published MQTT event to Kafka topic {} with key {}: {}", kafkaTopic, key, jsonNode);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for incoming messages
    }
}