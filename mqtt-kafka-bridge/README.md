# MQTT-Kafka Bridge

A service that bridges MQTT messages to Apache Kafka topics, enabling seamless integration between IoT devices and Kafka-based data pipelines.

## Overview

This bridge service subscribes to MQTT topics and forwards received messages to corresponding Kafka topics, acting as a reliable connector between IoT data sources and analytics platforms.

## Project Structure

The `src` directory will contain all source code for the MQTT-Kafka Bridge. This project is currently in planning phase and implementation files have not yet been created.

Please pay special attention to the **application.yml** file in the folder **resources**, maybe you need to make some adjustments to this file depending on which MQTT Topics you want to listen and convert them to Kafka messages. 

### Planned Components

* MQTT Client - For connecting to MQTT brokers and subscribing to topics
* Kafka Producer - For sending messages to Kafka topics
* Configuration Management - For managing connection settings and topic mappings
* Message Transformation - For converting between message formats
* Logging and Monitoring - For operational visibility and troubleshooting
* Error Handling - For resilient operation and recovery

## Setup and Installation

Once implemented, the service will be configured via environment variables or configuration files to set:

- MQTT broker connection settings
- Kafka broker connection settings
- Topic mapping configuration
- Logging levels
- Retry policies

## Running the Bridge

### Building the Application

```bash
# Build the project
mvn clean package
```

### Starting the Bridge

```bash
# Run with Maven
mvn spring-boot:run
```
