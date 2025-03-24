# Scale

## Overview
This Spring Boot application handles everything regarding the scale functionality. So put new request into a queue so the scale and the whole process knows who is currently weighing the syringe. 

## Features
- Listen to scale events
- Allows for reserve & unreseve scale
- Validates that the correct dose is used. 

## Getting Started

### Prerequisites
- JDK 21 or higher
- Maven 
- Message broker (Kafka)


### Installation
```bash
# Build the project
./mvnw clean install
```

### Configuration
Configure the application using `application.yml`:
