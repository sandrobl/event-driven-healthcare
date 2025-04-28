package com.eventdriven.healthcare.camundaorchestrator.config;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import com.eventdriven.healthcare.avro.NfcEvent;
import com.eventdriven.healthcare.avro.MQTTScaleEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.schema-registry-url}")
    private String schemaRegistryUrl;

    @Bean
    public ConsumerFactory<String, JsonNode> JsonConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Create a JsonDeserializer for JsonNode
        JsonDeserializer<JsonNode> valueDeserializer = new JsonDeserializer<>(JsonNode.class);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JsonNode> kafkaListenerJsonFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JsonNode> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(JsonConsumerFactory());
        factory.setBatchListener(false);
        return factory;
    }

    // Kafka Consumer for NfcEvent
    @Bean
    public ConsumerFactory<String, NfcEvent> ArvoNfcConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(props);
    }
    // Kafka Listener Container Factory for NfcEvent
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NfcEvent>
    kafkaListenerArvoNfcFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NfcEvent> f =
                new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(ArvoNfcConsumerFactory());
        return f;
    }

    // Kafka Consumer for MQTTScaleEvent
    @Bean
    public ConsumerFactory<String, MQTTScaleEvent> ArvoScaleConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    // Kafka Listener Container Factory for NfcEvent
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MQTTScaleEvent>
    kafkaListenerArvoScaleFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MQTTScaleEvent> f =
                new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(ArvoScaleConsumerFactory());
        return f;
    }
}