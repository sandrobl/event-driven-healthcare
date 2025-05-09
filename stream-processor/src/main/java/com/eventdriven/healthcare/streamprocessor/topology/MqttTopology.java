package com.eventdriven.healthcare.streamprocessor.topology;

import com.eventdriven.healthcare.avro.MQTTScaleEvent;
import com.eventdriven.healthcare.avro.EnrichedCheckInEvent;
import com.eventdriven.healthcare.streamprocessor.serialization.avro.AvroSerdes;
import com.eventdriven.healthcare.streamprocessor.serialization.dto.EnrichedPatient;
import com.eventdriven.healthcare.streamprocessor.serialization.dto.PatientMetricsEvent;
import com.eventdriven.healthcare.streamprocessor.serialization.dto.PatientStaticEvent;
import com.eventdriven.healthcare.streamprocessor.serialization.json.JsonNodeSerde;
import com.eventdriven.healthcare.avro.NfcEvent;
import com.eventdriven.healthcare.streamprocessor.util.NfcFormatter;
import com.eventdriven.healthcare.streamprocessor.util.ScaleFormatter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class MqttTopology {
    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    // support Instant, LocalDateTime, etc.
                    .registerModule(new JavaTimeModule())
                    // output ISO-strings instead of timestamps
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, JsonNode> stream = builder.stream(
                "smart-healthcare-data",
                Consumed.with(Serdes.String(), new JsonNodeSerde())
        );
        stream.print(Printed.<String, JsonNode>toSysOut().withLabel("Raw Data (smart-healthcare-data)"));

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
        patientStream.print(Printed.<String, JsonNode>toSysOut().withLabel("Raw Data (patientEvents-topic)"));

        // Process NFC events
        // -----------------
        KStream<String, JsonNode> nfcStream = branches.get("branch-nfc-events");
        nfcStream.print(Printed.<String, JsonNode>toSysOut().withLabel("branch-nfc-events"));

        KStream<String, JsonNode> eventFilteredNfcStream =
                nfcStream.filter((k, node) -> node.get("readingID").asInt() == 1);
        eventFilteredNfcStream.print(Printed.<String, JsonNode>toSysOut().withLabel("event-filtered-nfc-events"));

        KStream<String, ObjectNode> contentFilteredNfcStream = eventFilteredNfcStream.mapValues(node -> {
            ObjectNode out = JsonNodeFactory.instance.objectNode();
            out.put("location", node.path("location").asText());
            out.put("messageID", node.path("messageID").asInt());
            out.put("rawNfcId", node.path("ID").asText(null));
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

        eventTranslatedNfcStream.print(Printed.<String, NfcEvent>toSysOut().withLabel("translated-nfc-events"));

        // Create keyed stream using NFC ID as key
        KStream<String, NfcEvent> nfcKeyedStream = eventTranslatedNfcStream
                .map((key, nfc) -> KeyValue.pair(nfc.getNfcID().toString(), nfc));

        // Apply 5-second session window
        SessionWindows sessionWindows =
                SessionWindows.ofInactivityGapWithNoGrace(Duration.ofSeconds(5));

        // Aggregation
        KTable<Windowed<String>, NfcEvent> nfcSessions = nfcKeyedStream
                .groupByKey(Grouped.with(Serdes.String(), AvroSerdes.nfcEvent("http://localhost:9010", false)))
                .windowedBy(sessionWindows)
                .reduce((oldValue, newValue) -> newValue); // Simply keep the latest value

        // Debug output for the windowed sessions
        nfcSessions.toStream()
                .map((windowedKey, value) -> KeyValue.pair(
                        windowedKey.key() + "@" + windowedKey.window().startTime() + "-" + windowedKey.window().endTime(),
                        value))
                .print(Printed.<String, NfcEvent>toSysOut().withLabel("DEBUG - NFC SESSION WINDOWS"));


        // Send to topic
        nfcSessions.toStream()
                .map((windowedKey, value) -> KeyValue.pair(windowedKey.key(), value))
                .to(
                        "nfc-events",
                        Produced.with(
                                Serdes.String(),
                                AvroSerdes.nfcEvent("http://localhost:9010", false))
                );

//        eventTranslatedNfcStream
//                .map(
//                        (key, nfc) -> {
//                            // use the NFC ID as the key
//                            String nfcId = nfc.getNfcID().toString();
//                            return KeyValue.pair(nfcId, nfc);
//                        }
//                )
//                .to(
//                "nfc-events",
//                Produced.with(
//                        Serdes.String(),
//                        AvroSerdes.nfcEvent("http://localhost:9010", false))
//        );

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
                    out.put("weight", node.path("weight").asInt());
                    out.put("messageID", node.path("messageID").asInt());
                    return out;
                });
        contentFilteredScaleStream.print(Printed.<String, ObjectNode>toSysOut().withLabel("content-filtered-scale-events"));

        KStream<String, MQTTScaleEvent> eventTranslatedScaleStream =
                contentFilteredScaleStream.mapValues(node -> {
                    float rawScaleWeight =
                            (float) node.get("weight").asDouble(0f);
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

        // Patient Data kTable enrichments
        // --------------------------------
        // Static data
        KTable<String, JsonNode> rawPatientStaticTable = builder.table(
                "patient-static-topic",
                Consumed.with(Serdes.String(), new JsonNodeSerde())
        );
        KTable<String, PatientStaticEvent> patientStaticTable = rawPatientStaticTable
                .mapValues(json -> MAPPER.convertValue(json, PatientStaticEvent.class));

        // Dynamic metrics
        KTable<String, JsonNode> rawPatientMetricsTable = builder.table(
                "patient-metrics-topic",
                Consumed.with(Serdes.String(), new JsonNodeSerde()),
                Materialized.as("patient-metrics-store")
        );
        KTable<String, PatientMetricsEvent> patientMetricsTable = rawPatientMetricsTable
                .mapValues(json -> {
                    PatientMetricsEvent e = new PatientMetricsEvent();
                    e.setNfcId(json.get("nfcId").asText());
                    if (!json.get("height").isNull()) e.setHeight(json.get("height").asDouble());
                    if (!json.get("weight").isNull()) e.setWeight(json.get("weight").asDouble());
                    if (!json.get("insulinSensitivityFactor").isNull())
                        e.setInsulinSensitivityFactor(json.get("insulinSensitivityFactor").asDouble());
                    // parse timestamp as epoch‐seconds (it's a scientific notation number)
                    long epochSec = (long) json.get("timestamp").asDouble();
                    e.setTimestamp(Instant.ofEpochSecond(epochSec));
                    return e;
                });

        // Join static + metrics → enriched patient
        KTable<String, JsonNode> patientTable = patientStaticTable
                .leftJoin(patientMetricsTable, EnrichedPatient::from)
                .mapValues(MAPPER::valueToTree,
                        Materialized.<String, JsonNode, KeyValueStore<Bytes, byte[]>>as("patient-table-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonNodeSerde())
                );


        patientTable.toStream().print(Printed.<String, JsonNode>toSysOut()
                .withLabel("ENRICHED-PATIENT-TABLE")
                .withKeyValueMapper((k, v) -> k + " : " + v));


        // ---- Enrich processed NFC stream and emit Avro enriched events ----
        // Join NFC events with patient data
        KStream<String, NfcEvent> nfcEventStream = builder.stream(
                "nfc-events",
                Consumed.with(Serdes.String(), AvroSerdes.nfcEvent("http://localhost:9010", false))
        );

        KStream<String, EnrichedCheckInEvent> enrichedCheckInEventStream =
                nfcEventStream
                        .leftJoin(
                                patientTable,
                                (nfc, patient) -> {
                                    if (patient == null) {
                                        // return null so we can filter it out
                                        return null;
                                    }
                                    // Convert the JsonNode to a Patient object
                                    EnrichedPatient p = MAPPER.convertValue(patient, EnrichedPatient.class);
                                    EnrichedCheckInEvent.Builder builderEnrChInEvt = EnrichedCheckInEvent.newBuilder()
                                            .setLocation(nfc.getLocation())
                                            .setMessageID(nfc.getMessageID())
                                            .setNfcID(nfc.getNfcID())
                                            .setName(p.getName())
                                            .setFirstname(p.getFirstname());
                                    if (p.getHeight() != null) builderEnrChInEvt.setHeight(p.getHeight());
                                    if (p.getWeight() != null) builderEnrChInEvt.setWeight(p.getWeight());
                                    if (p.getInsulinSensitivityFactor() != null)
                                        builderEnrChInEvt.setInsulinSensitivityFactor(p.getInsulinSensitivityFactor());
                                    if (p.getTimestamp() != null) builderEnrChInEvt.setTimestamp(p.getTimestamp());
                                    return builderEnrChInEvt.build();
                                }
                        )
                        .filter((key, enriched) -> enriched != null);

        enrichedCheckInEventStream.print(Printed.<String, EnrichedCheckInEvent>toSysOut()
                .withLabel("ENRICHED-CHECKIN-EVENTS")
                .withKeyValueMapper((k, v) -> k + " : " + v));

        enrichedCheckInEventStream.to(
                "nfc-events-enriched",
                Produced.with(Serdes.String(), AvroSerdes.enrichedCheckInEvent("http://localhost:9010", false))
        );

        // Process of Adding all insulin doses per Patient (NFC ID
        // --------------------------------

        // 1) First build the lookup KTable: correlationId → nfcID
        KTable<String, String> correlationToNfcTable = patientStream
                // Filter for any message containing patient info with valid NFC ID
                .filter((corrId, node) ->
                        node.has("patient") &&
                                node.get("patient").has("nfcID") &&
                                !node.get("patient").get("nfcID").isNull() &&
                                !node.get("patient").get("nfcID").asText().equals("null"))
                .peek((key, value) -> System.out.println("FOUND PATIENT NODE: " + key +
                        ", nfcID: " + value.get("patient").get("nfcID").asText()))
                .mapValues(node -> node.get("patient").get("nfcID").asText())
                .toTable(
                        Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("correlation-nfc-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                );
        correlationToNfcTable.toStream().print(Printed.<String, String>toSysOut().withLabel("DEBUG - correlationToNfcTable"));

        // 2) Extract insulin doses from messages - this stays the same
        KStream<String, Double> insulinDoseStream = patientStream
                .filter((corrId, node) -> node.has("insulinDoses"))
                .peek((key, value) -> System.out.println("FOUND INSULIN DOSE: " + key +
                        ", dose: " + value.get("insulinDoses").asDouble()))
                .mapValues(node -> node.get("insulinDoses").asDouble());
        insulinDoseStream.print(Printed.<String, Double>toSysOut().withLabel("DEBUG - insulinDoseStream"));

        // 3) Join the dose stream to the lookup table to enrich with nfcID
        KStream<String, KeyValue<String, Double>> patientDoseMapping = insulinDoseStream
                .join(correlationToNfcTable,
                        (dose, nfcID) -> KeyValue.pair(nfcID, dose),
                        Joined.with(
                                Serdes.String(),  // correlation ID key
                                Serdes.Double(),  // insulin dose value
                                Serdes.String()   // nfcID value from table
                        )
                );
        patientDoseMapping.print(Printed.<String, KeyValue<String, Double>>toSysOut().withLabel("DEBUG - patientDoseMapping"));

        // 4) Re-key by patientId and drop the Pair wrapper
        KStream<String, Double> byPatient = patientDoseMapping
                .selectKey((corrId, pair) -> pair.key)
                .mapValues(pair -> pair.value);

        byPatient.print(Printed.<String, Double>toSysOut().withLabel("DEBUG - byPatient"));


        // 5) Finally, group by patientId and sum (no Windowing)
        KTable<String, Double> totalInsulinPerPatient = byPatient
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(
                        Double::sum,
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("total-insulin-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                );

        totalInsulinPerPatient.toStream().print(Printed.<String, Double>toSysOut().withLabel("DEBUG - totalInsulinPerPatient"));

        // With windowing

        // TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofDays(1));
        // For debugging purposes, we  use a smaller window size
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(120));

        KTable<Windowed<String>, Double> totalInsulinPerPatientWindowed = byPatient
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .windowedBy(timeWindows)
                .reduce(
                        Double::sum,
                        Materialized.<String, Double, WindowStore<Bytes, byte[]>>as("total-insulin-windowed-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                );

        totalInsulinPerPatientWindowed.toStream()
                .map((windowedKey, value) -> KeyValue.pair(
                        windowedKey.key() + "@" + windowedKey.window().start() + "-" + windowedKey.window().end(),
                        value))
                .print(Printed.<String, Double>toSysOut().withLabel("DEBUG - totalInsulinPerPatient Windowed"));

        KTable<String, Double> totalInsulinWindowedFlat = totalInsulinPerPatientWindowed
                .toStream()
                .map((windowedKey, value) ->
                        KeyValue.pair(
                                windowedKey.key() +
                                        "@" +
                                        windowedKey.window().start() + "-" +
                                        windowedKey.window().end(),
                                value
                        )
                )
                .toTable(
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("total-insulin-windowed-kvstore")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                );

        // Using Sliding (hopping) window to produce average Weight change
        // ----------------------------------------------------------------
        builder.addStateStore(
                Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore("last-weight-store"),
                        Serdes.String(),
                        Serdes.Double()
                )
        );
        // 1) Turn the KTable back into a KStream of (nfcId, weight)
        KStream<String, Double> weightStream = patientMetricsTable
                .toStream()
                .mapValues(metrics -> metrics.getWeight());

        // 2) TransformValues to remember each patient's last weight
        KStream<String, Double> deltaStream = weightStream.transformValues(
                        () -> new ValueTransformerWithKey<String,Double,Double>() {
                            private KeyValueStore<String,Double> lastWeight;

                            @Override
                            public void init(ProcessorContext ctx) {
                                lastWeight = ctx.getStateStore("last-weight-store");
                            }

                            @Override
                            public Double transform(String key, Double newWeight) {
                                Double prev = lastWeight.get(key);
                                lastWeight.put(key, newWeight);
                                // first event has no delta
                                if (prev == null) return null;
                                return newWeight - prev;
                            }

                            @Override
                            public void close() { }
                        },
                        Named.as("compute-delta"),
                        "last-weight-store"
                )
                .filter((k, delta) -> delta != null);   // drop the very first event per patient

        // 3) Hopping window: size = 24 h, advance = 1 h
        TimeWindows hop24h = TimeWindows
                .ofSizeWithNoGrace(Duration.ofHours(24))
                .advanceBy(Duration.ofHours(1));

        // 5) Materialize the windowed aggregator
        KTable<Windowed<String>, JsonNode> windowedStats = deltaStream
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .windowedBy(hop24h)
                .aggregate(
                        // initializer returns an ObjectNode (a JsonNode subtype)
                        () -> {
                            ObjectNode o = JsonNodeFactory.instance.objectNode();
                            o.put("sum",   0.0);
                            o.put("count", 0L);
                            o.put("avg",   0.0);
                            return o;
                        },
                        // adder takes (key, delta, oldJson) where oldJson is declared as JsonNode…
                        (patientId, delta, oldJson) -> {
                            // …so cast it back to ObjectNode to mutate:
                            ObjectNode o = (ObjectNode) oldJson;
                            double sum   = o.get("sum").asDouble() + delta;
                            long   count = o.get("count").asLong()   + 1;
                            o.put("sum",   sum);
                            o.put("count", count);
                            o.put("avg",   sum / count);
                            return o;
                        },
                        // still materialize as JsonNode so your existing JsonNodeSerde works
                        Materialized.<String, JsonNode, WindowStore<Bytes, byte[]>>as("weight-delta-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonNodeSerde())
                );
        // 6) Map to average and flatten into a regular KV store for querying
        KTable<String, Double> avgDeltaFlat = windowedStats
                .toStream()
                .map((windowedKey, json) -> {
                    // build the new key
                    String flatKey = windowedKey.key()
                            + "@"
                            + windowedKey.window().start()
                            + "-"
                            + windowedKey.window().end();

                    // pull out the avg field from the JsonNode
                    double avg = json.has("avg")
                            ? json.get("avg").asDouble()
                            : 0.0;

                    return KeyValue.pair(flatKey, avg);
                })
                .toTable(
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("avg-weight-change-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                );
        return builder.build();
    }
}