package com.eventdriven.healthcare.streamprocessor.serialization.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonNodeSerde extends Serdes.WrapperSerde<JsonNode> {
  public JsonNodeSerde() {
    super(new JsonNodeSerializer(), new JsonNodeDeserializer());
  }
}