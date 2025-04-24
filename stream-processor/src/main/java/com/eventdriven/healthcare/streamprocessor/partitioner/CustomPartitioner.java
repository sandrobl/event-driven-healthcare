package com.eventdriven.healthcare.streamprocessor.partitioner;

import magicalpipelines.model.TranslatedGaze;
import org.apache.kafka.streams.processor.StreamPartitioner;

public class CustomPartitioner implements StreamPartitioner<String, TranslatedGaze> {

    @Override
    public Integer partition(String topic, String key, TranslatedGaze value, int numPartitions) {
        if (key.equals("high CL")) {
            return 1; // Send all records with the "high CL" key to partition 1
        } else {
            return 0; // Send all records with the "low CL" key to partition 0
        }
    }
}