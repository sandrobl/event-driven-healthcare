# Insulin Calculator REST Service

A REST API service for calculating insulin doses.

## Description

This service provides endpoints for insulin dose calculations based on various parameters such as blood glucose levels, carbohydrate intake, and other factors that affect insulin requirements.

## Getting Started

### Prerequisites

- Java (version X.X or higher)
- Maven or Gradle
- [Other dependencies]

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/insulin-calculator-rest.git

# Navigate to the project directory
cd insulin-calculator-rest

# Build the project
mvn clean install
```

## Running the Service

```bash
# Run the service
mvn spring-boot:run
```

The service will be available at `http://localhost:8080`.

## Service Operation Modes

While the service is running, you can control its response time behavior through the console:

- Type `s` or `S` and press Enter to switch to "Slow Mode" - this artificially slows down API responses
- Type `n` or `N` and press Enter to switch back to "Normal Mode" - this returns the service to standard response times

This feature is useful for testing client-side timeout handling, demonstrating resilience patterns, or simulating high-load conditions.


## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/calculateInsulin` | POST | Calculate insulin dose based on input parameters |

The parameters can be found in the **InsulinCalculatedRequest.java** file, to see what the response will be consult **InsulinCalculationResposne.java**. 

## API Documentation

Detailed API documentation is available at `http://localhost:8080/swagger-ui.html` when the service is running.

## Technologies

- Spring Boot
- [Other technologies]

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

[License information]
