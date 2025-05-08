package com.eventdriven.healthcare.streamprocessor;

import com.eventdriven.healthcare.streamprocessor.service.MonitorService;
import com.eventdriven.healthcare.streamprocessor.topology.MqttTopology;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

class EventProcessingApp {
    public static void main(String[] args) throws InterruptedException {
        // 1. Ensure required topics exists
        // ---------------------------------
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        try (AdminClient admin = AdminClient.create(adminProps)) {
            List<NewTopic> desired = List.of(
                    new NewTopic("smart-healthcare-data",  1, (short)1),
                    new NewTopic("nfc-events",             1, (short)1),
                    new NewTopic("scale-events",           1, (short)1),
                    new NewTopic("nfc-events-enriched", 1, (short)1),
                    new NewTopic("patient-static-topic", 1, (short)1),
                    new NewTopic("patient-metrics-topic", 1, (short)1)
                    );

            Set<String> existing = admin.listTopics()
                    .names()
                    .get();

            List<NewTopic> toCreate = desired.stream()
                    .filter(t -> !existing.contains(t.name()))
                    .collect(Collectors.toList());

            if (!toCreate.isEmpty()) {
                admin.createTopics(toCreate).all().get();
                System.out.println("Created topics: " +
                        toCreate.stream().map(NewTopic::name).collect(Collectors.joining(", ")));
            } else {
                System.out.println("All topics already exist; nothing to do.");
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 2. Build Topology and Start Kafka Streams
        // ------------------------------------------
        Topology topology = MqttTopology.build();
        Properties streamsConfig = new Properties();
        streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG,   "stream-processor");
        streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // … your other serde & schema-registry config …

        KafkaStreams streams = new KafkaStreams(topology, streamsConfig);

        // ——— 3) Add shutdown hook & latch ———
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down streams…");
            streams.close();
            latch.countDown();
        }));

        streams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING) {
                System.out.println("Start monitoring service…");
                new MonitorService(new HostInfo("localhost", 7070), streams).start();
            }
        });

        // ——— 4) Start and block forever ———
        System.out.println("Starting Kafka Streams application…");
        streams.start();

        // now main() will wait here until someone hits Ctrl-C
        latch.await();
        System.out.println("Application has exited.");
    }
}
