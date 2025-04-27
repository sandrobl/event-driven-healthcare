package com.eventdriven.healthcare.streamprocessor.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class JsonNodeDeserializer implements Deserializer<JsonNode> {
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public JsonNode deserialize(String topic, byte[] bytes) {
    try {
      return (bytes == null || bytes.length == 0)
              ? null
              : mapper.readTree(bytes);
    } catch (Exception e) {
      throw new RuntimeException("Failed to deserialize JsonNode", e);
    }
  }
}