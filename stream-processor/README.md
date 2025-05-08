# Stream Processor Service

## Overview
The Stream Processor is a Kafka Streams application that processes healthcare event data in real-time. It primarily handles patient interaction events, insulin tracking, and data enrichment from various healthcare devices.

## Features

- **Real-time Event Processing**: Processes NFC tag readings, scale measurements, and patient data events
- **Insulin Dose Tracking**: Aggregates and maintains a running total of insulin doses per patient
- **Data Transformation**: Converts raw device data into structured events
- **Data Enrichment**: Joins patient information with device readings
- **Interactive Queries**: Exposes real-time insulin totals through REST endpoints

## Architecture

The application consists of these key components:

- **Kafka Streams Topology**: Core processing logic defined in `MqttTopology`
- **State Stores**: Maintains correlation IDs to NFC mappings and patient insulin totals
- **Avro Serialization**: Uses Confluent Avro serialization for event schemas


## Setup and Configuration

### Prerequisites
- Java 21
- Apache Kafka
- Confluent Schema Registry
- Maven

### Building the Application

## Setup and Configuration

### Prerequisites
- Java 21
- Apache Kafka
- Confluent Schema Registry
- Maven

### Building the Application

```bash
mvn clean package
```
