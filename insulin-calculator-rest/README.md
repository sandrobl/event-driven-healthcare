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

### Example 1: Result around 3ml

Let's assume a patient with high blood glucose and some insulin resistance.

* **Blood Glucose:** 32 mmol/L
* **Target Blood Glucose Level:** 5 mmol/L
* **Next Meal Carbohydrates:** 100 grams
* **Insulin To Carbohydrate Ratio:** 3
* **Patient Insulin Sensitivity Factor:** 0.1

**Calculation:**

1.  **Carbohydrate Coverage Dose:** `100 / 3 = 33.33 units`
2.  **Correction Dose:** `(32 - 5) / 0.1 = 270 units`
3.  **Total Bolus Dose:** `33.33 + 270 = 303.33 units`
4.  **Result in ml:** `303.33 / 100 = 3.03 ml`

### Example 2: Result around 8ml

Let's assume a patient with very high blood glucose and significant insulin resistance.

* **Blood Glucose:** 40 mmol/L
* **Target Blood Glucose Level:** 5 mmol/L
* **Next Meal Carbohydrates:** 200 grams
* **Insulin To Carbohydrate Ratio:** 2
* **Patient Insulin Sensitivity Factor:** 0.05

**Calculation:**

1.  **Carbohydrate Coverage Dose:** `200 / 2 = 100 units`
2.  **Correction Dose:** `(40 - 5) / 0.05 = 700 units`
3.  **Total Bolus Dose:** `100 + 700 = 800 units`
4.  **Result in ml:** `800 / 100 = 8 ml`


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
