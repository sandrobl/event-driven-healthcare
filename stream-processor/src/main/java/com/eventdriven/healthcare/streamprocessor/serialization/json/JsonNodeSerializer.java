package com.eventdriven.healthcare.streamprocessor.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonNodeSerializer implements Serializer<JsonNode> {
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public byte[] serialize(String topic, JsonNode data) {
    try {
      return data == null ? null : mapper.writeValueAsBytes(data);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize JsonNode", e);
    }
  }
}