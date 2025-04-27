package com.eventdriven.healthcare.streamprocessor.serialization.avro;

import com.eventdriven.healthcare.avro.MQTTScaleEvent;
import com.eventdriven.healthcare.avro.NfcEvent;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;

import java.util.Collections;
import java.util.Map;

public class AvroSerdes {

  private static <T extends SpecificRecord> Serde<T> make(String schemaRegistryUrl, boolean isKey) {
    Map<String, String> cfg =
            Collections.singletonMap(
                    AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                    schemaRegistryUrl
            );
    SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
    serde.configure(cfg, isKey);
    return serde;
  }

  public static Serde<NfcEvent> nfcEvent(String url, boolean isKey) {
    return make(url, isKey);
  }

  public static Serde<MQTTScaleEvent> scaleEvent(String url, boolean isKey) {
    return make(url, isKey);
  }
}