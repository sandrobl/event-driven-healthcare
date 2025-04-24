package com.eventdriven.healthcare.streamprocessor.serialization.json;

import com.google.gson.Gson;
import com.eventdriven.healthcare.streamprocessor.serialization.Gaze;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

class GazeSerializer implements Serializer<Gaze> {
  private Gson gson = new Gson();

  @Override
  public byte[] serialize(String topic, Gaze gaze) {
    if (gaze == null) return null;
    return gson.toJson(gaze).getBytes(StandardCharsets.UTF_8);
  }
}
