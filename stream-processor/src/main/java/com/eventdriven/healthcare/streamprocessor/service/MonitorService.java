package com.eventdriven.healthcare.streamprocessor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorService {

    private final HostInfo hostInfo;
    private final KafkaStreams streams;

    // Logger
    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    public MonitorService(HostInfo hostInfo, KafkaStreams streams) {
        this.hostInfo = hostInfo;
        this.streams = streams;
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");  // serves index.html from resources/public
        }).start(hostInfo.port());

        app.get("/patientTable", this::getPatientTable);
        app.get("/totalInsulinPerPatient", this::getTotalInsulin);
        app.get("/totalInsulinWindowed", this::getTotalInsulinWindowed);
        app.get("/weightChange", this::getWeightChange);

    }

    // 1) Enriched patients
    void getPatientTable(Context ctx) {
        Map<String, JsonNode> out = new HashMap<>();
        ReadOnlyKeyValueStore<String, JsonNode> store = streams.store(
                StoreQueryParameters.fromNameAndType(
                        "patient-table-store",
                        QueryableStoreTypes.keyValueStore())
        );
        try (KeyValueIterator<String, JsonNode> iter = store.all()) {
            iter.forEachRemaining(kv -> out.put(kv.key, kv.value));
        }
        ctx.json(out);
    }

    // 2) Non-windowed insulin sums
    void getTotalInsulin(Context ctx) {
        Map<String, Double> out = new HashMap<>();
        ReadOnlyKeyValueStore<String, Double> store = streams.store(
                StoreQueryParameters.fromNameAndType(
                        "total-insulin-store",
                        QueryableStoreTypes.keyValueStore())
        );
        try (KeyValueIterator<String, Double> iter = store.all()) {
            iter.forEachRemaining(kv -> out.put(kv.key, kv.value));
        }
        ctx.json(out);
    }

    // 3) Windowed insulin sums (flat KV store)
    void getTotalInsulinWindowed(Context ctx) {
        Map<String, Double> out = new HashMap<>();
        ReadOnlyKeyValueStore<String, Double> store = streams.store(
                StoreQueryParameters.fromNameAndType(
                        "total-insulin-windowed-kvstore",
                        QueryableStoreTypes.keyValueStore())
        );
        try (KeyValueIterator<String, Double> iter = store.all()) {
            iter.forEachRemaining(kv -> out.put(kv.key, kv.value));
        }
        ctx.json(out);
    }

    void getWeightChange(Context ctx) {
        // 1) Read the store as Double values
        ReadOnlyKeyValueStore<String, Double> store =
                streams.store(
                        StoreQueryParameters.fromNameAndType(
                                "avg-weight-change-store",
                                QueryableStoreTypes.keyValueStore()
                        )
                );

        // 2) Pull entries into a list and sort descending by the Double
        List<Map.Entry<String, Double>> entries = new ArrayList<>();
        try (KeyValueIterator<String, Double> it = store.all()) {
            it.forEachRemaining(kv -> entries.add(Map.entry(kv.key, kv.value)));
        }
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 3) Build a JSON array of { window, avgChange }
        ArrayNode out = JsonNodeFactory.instance.arrayNode();
        for (var e : entries) {
            ObjectNode o = JsonNodeFactory.instance.objectNode();
            o.put("window",    e.getKey());
            o.put("avgChange", e.getValue());
            out.add(o);
        }

        // 4) Return it
        ctx.json(out);
    }
}