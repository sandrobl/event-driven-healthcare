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
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

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


        KStream<String, JsonNode> patientStream = builder.stream(
                "patientEvents-topic",
                Consumed.with(Serdes.String(), new JsonNodeSerde())
        );
        stream.print(Printed.<String, JsonNode>toSysOut().withLabel("Raw Data"));       

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



        // 1) Build the lookup KTable: correlationId → patientId
        KTable<String,String> correlationToPatientTable = patientStream
                .filter((corrId, node) ->
                        "displayPatientData".equalsIgnoreCase(node.get("messageType").asText()))
                .mapValues(node -> node.get("patient").get("id").asText())
                .toTable(
                        Materialized.<String,String,KeyValueStore<Bytes,byte[]>>as("correlation-patient-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );

        // 2) Extract insulin doses as a KStream keyed by correlationId
        KStream<String,Double> insulinDoseStream = patientStream
                .filter((corrId, node) ->
                        "displayInsulinDose".equalsIgnoreCase(node.get("messageType").asText()))
                .mapValues(node -> {
                    // adjust if your JSON nests payload.insulinDoses
                    return node.has("payload")
                            ? node.get("payload").get("insulinDoses").asDouble()
                            : node.get("insulinDoses").asDouble();
                });

        // 3) Join the dose stream to the lookup table to enrich with patientId
        //    Resulting stream key is still correlationId, value is a Pair(patientId, dose)
        KStream<String,KeyValue<String,Double>> enriched = insulinDoseStream
                .join(correlationToPatientTable,
                        /* valueJoiner */ (dose, patientId) -> KeyValue.pair(patientId, dose),
                        /* materialize serde */ Joined.with(
                                Serdes.String(),         // existing key serde (correlationId)
                                Serdes.Double(),         // insulin dose serde
                                Serdes.String()          // patientId serde
                        )
                );

        // 4) Re-key by patientId and drop the Pair wrapper
        KStream<String,Double> byPatient = enriched
                .selectKey((corrId, pair) -> pair.key)
                .mapValues(pair -> pair.value);

        // 5) Finally, group by patientId and sum
        KTable<String,Double> totalInsulinPerPatient = byPatient
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(
                        Double::sum,
                        Materialized.<String,Double,KeyValueStore<Bytes,byte[]>>as("total-insulin-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                );

        // 1. Convert the table to a stream of updates
        KStream<String, Double> totalsStream = totalInsulinPerPatient.toStream();

        // 2. Send to a dedicated topic
        totalsStream.to(
                "patient-insulin-totals",
                Produced.with(Serdes.String(), Serdes.Double())
        );



        return builder.build();
    }
}