# Insulin Calculator Service

## Overview

The Insulin Calculator Service is a component of the event-driven healthcare system. It listens to patient events from a Kafka topic, calculates the required insulin doses, and sends the calculated requests to another Kafka topic.

## Technologies Used

- Java
- Spring Boot
- Apache Kafka
- Maven

## Project Structure

- `src/main/java/com/eventdriven/healthcare/insulincalculator/service/ConsumerService.java`: Contains the Kafka consumer logic and insulin dose calculation.
- `src/main/java/com/eventdriven/healthcare/insulincalculator/model/InsulinCalculationRequest.java`: Model for insulin calculation requests.
- `src/main/java/com/eventdriven/healthcare/insulincalculator/service/ProducerService.java`: Sends the calculated insulin requests to a Kafka topic.

## How to Run

1. **Clone the repository:**
   ```sh
   git clone <repository-url>
   cd insulin-calculator
   
2. **Build the project:**  
   ```sh
   mvn clean install  
   
3. **Run the application:**  
   ```sh
   mvn spring-boot:run  

4. **DTO Architecture:**
   ````java
    int patientID;
    float bloodGlucose;
    float weight;
    float height;
    float insulinDoses; // Insulin doses in ml
    float nextMealCarbohydrates;
    float insulinToCarbohydrateRatio; // Insulin-to-Carbohydrate Ratio (grams/unit)
    float insulinSensitivityFactor; // Insulin Sensitivity Factor (mg/dL per unit)
    float targetBloodGlucoseLevel; // Target blood glucose level (mg/dL)

## Configuration
The application can be configured using the application.properties file located in src/main/resources. Key configurations include:  
- spring.kafka.bootstrap-servers: Kafka server address.
- spring.kafka.patientEvents-topic: Kafka topic for patient events.
- spring.kafka.group-id: Kafka consumer group ID.

## Usage
The service listens to patient events on the configured Kafka topic, calculates the insulin doses based on the received data, and sends the calculated requests to another Kafka topic.  
