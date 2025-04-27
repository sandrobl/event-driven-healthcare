package com.eventdriven.healthcare.streamprocessor.topology;

import com.eventdriven.healthcare.avro.MQTTScaleEvent;
import com.eventdriven.healthcare.streamprocessor.serialization.avro.AvroSerdes;
import com.eventdriven.healthcare.streamprocessor.serialization.json.JsonNodeSerde;
import com.eventdriven.healthcare.avro.NfcEvent;
import com.eventdriven.healthcare.streamprocessor.util.NfcFormatter;
import com.eventdriven.healthcare.streamprocessor.util.ScaleFormatter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Map;

public class MqttTopology {

    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, JsonNode> stream = builder.stream(
                "smart-healthcare-data",
                Consumed.with(Serdes.String(), new JsonNodeSerde())
        );
        stream.print(Printed.<String, JsonNode>toSysOut().withLabel("Raw Data"));

        Map<String, KStream<String, JsonNode>> branches = stream
                .split(Named.as("branch-"))
                .branch((key, node) -> "nfc".equalsIgnoreCase(node.get("type").asText()), Branched.as("nfc-events"))
                .branch((key, node) -> "load_cell".equalsIgnoreCase(node.get(
                        "type").asText()), Branched.as("scale-events"))
                .noDefaultBranch();

        // Process NFC events
        // -----------------
        KStream<String, JsonNode> nfcStream = branches.get("branch-nfc-events");
        nfcStream.print(Printed.<String, JsonNode>toSysOut().withLabel("branch-nfc-events"));

        KStream<String, JsonNode> eventFilteredNfcStream =
                nfcStream.filter((k, node) -> node.get("readingID").asInt() == 1);
        eventFilteredNfcStream.print(Printed.<String, JsonNode>toSysOut().withLabel("event-filtered-nfc-events"));

        KStream<String, ObjectNode> contentFilteredNfcStream = eventFilteredNfcStream.mapValues(node -> {
            ObjectNode out = JsonNodeFactory.instance.objectNode();
            out.put("location",  node.path("location").asText());
            out.put("messageID", node.path("messageID").asInt());
            out.put("rawNfcId",  node.path("ID").asText(null));
            return out;
        });
        contentFilteredNfcStream.print(Printed.<String, ObjectNode>toSysOut().withLabel("content-filtered-nfc-events"));

        KStream<String, NfcEvent> eventTranslatedNfcStream = contentFilteredNfcStream.mapValues(node -> {
            String rawNfcId = node.get("rawNfcId").asText(null);
            String formattedNfcId = NfcFormatter.format(rawNfcId);

            NfcEvent nfcEvent = new NfcEvent();
            nfcEvent.setLocation(node.get("location").asText());
            nfcEvent.setMessageID(node.get("messageID").asInt());
            nfcEvent.setNfcID(formattedNfcId);
            return nfcEvent;
        });


        eventTranslatedNfcStream.to(
                "nfc-events",
                Produced.with(
                        Serdes.String(),
                        AvroSerdes.nfcEvent("http://localhost:9010", false))
        );

        // Process Scale events
        // -------------------
        KStream<String, JsonNode> scaleStream = branches.get("branch-scale-events");
        scaleStream.print(Printed.<String, JsonNode>toSysOut().withLabel("scale-events"));

        KStream<String, JsonNode> eventFilteredScaleStream =
                scaleStream.filter((k, node) -> node.get("weight").asInt() >= 5);
        eventFilteredScaleStream.print(Printed.<String, JsonNode>toSysOut().withLabel("event-filtered-scale-events"));

        KStream<String, ObjectNode> contentFilteredScaleStream =
                eventFilteredScaleStream.mapValues(node -> {
            ObjectNode out = JsonNodeFactory.instance.objectNode();
            out.put("weight",   node.path("weight").asInt());
            out.put("messageID", node.path("messageID").asInt());
            return out;
        });
        contentFilteredScaleStream.print(Printed.<String, ObjectNode>toSysOut().withLabel("content-filtered-scale-events"));

        KStream<String, MQTTScaleEvent> eventTranslatedScaleStream =
                contentFilteredScaleStream.mapValues(node -> {
                    float rawScaleWeight =
                            (float)node.get("weight").asDouble(0f);
                    float formattedScaleWeight = ScaleFormatter.format(rawScaleWeight);

                    MQTTScaleEvent scaleEvent = new MQTTScaleEvent();
                    scaleEvent.setMessageID(node.get("messageID").asInt());
                    scaleEvent.setWeight(formattedScaleWeight);
                    return scaleEvent;
        });
        eventTranslatedScaleStream.print(Printed.<String, MQTTScaleEvent>toSysOut().withLabel("content-transformed-scale-events"));


        eventTranslatedScaleStream.to(
                "scale-events",
                Produced.with(
                        Serdes.String(),
                        AvroSerdes.scaleEvent("http://localhost:9010",
                                false))
        );

        return builder.build();
    }
}