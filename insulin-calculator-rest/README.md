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

### Extreme Examples with Specified Insulin Sensitivity Factors (2-10 ml Range)

**Patient 1: Insulin Sensitivity Factor = 3**

Let's assume an extremely high blood glucose level and a very large meal.

* **Blood Glucose:** 50 mmol/L (Dangerously high)
* **Target Blood Glucose Level:** 4 mmol/L
* **Next Meal Carbohydrates:** 400 grams (Extremely large meal)
* **Insulin To Carbohydrate Ratio:** 3 (Very low ratio)
* **Patient Insulin Sensitivity Factor:** 3 (1 unit lowers blood glucose by 3 mmol/L)

**Calculation:**

1.  **Carbohydrate Coverage Dose:** `400 grams / 3 = 133.33 units`
2.  **Correction Dose:** `(50 mmol/L - 4 mmol/L) / 3 = 46 / 3 = 15.33 units`
3.  **Total Bolus Dose:** `133.33 + 15.33 = 148.66 units`
4.  **Result in ml:** `148.66 units / 100 = 1.49 ml`

To get this patient into the 2-10 ml range, the parameters would need to be even more extreme, which might represent medically improbable scenarios for a single bolus dose.

**Patient 2: Insulin Sensitivity Factor = 1.5**

Let's assume an extremely high blood glucose level and a very large meal with significant insulin resistance.

* **Blood Glucose:** 60 mmol/L (Extremely dangerously high)
* **Target Blood Glucose Level:** 4 mmol/L
* **Next Meal Carbohydrates:** 500 grams (Unrealistically high)
* **Insulin To Carbohydrate Ratio:** 3 (Very low ratio)
* **Patient Insulin Sensitivity Factor:** 1.5 (1 unit lowers blood glucose by 1.5 mmol/L)

**Calculation:**

1.  **Carbohydrate Coverage Dose:** `500 grams / 3 = 166.67 units`
2.  **Correction Dose:** `(60 mmol/L - 4 mmol/L) / 1.5 = 56 / 1.5 = 37.33 units`
3.  **Total Bolus Dose:** `166.67 + 37.33 = 204 units`
4.  **Result in ml:** `204 units / 100 = 2.04 ml`

This example for Patient 2 just crosses the 2ml threshold and represents a highly unusual and extreme scenario.

**Important Note:** These examples use parameters that are far beyond typical ranges and are meant to illustrate the functionality of the service. 

**Patient 3: No insulin required**
* **Blood Glucose:** 6 mmol/L (Normal)
* **Target Blood Glucose Level:** 4 mmol/L
* **Next Meal Carbohydrates:** 500 grams (Unrealistically high)
* **Insulin To Carbohydrate Ratio:** 3 (Very low ratio)
* **Patient Insulin Sensitivity Factor:** 1.5 (1 unit lowers blood glucose by 1.5 mmol/L)

This patient does not require any insulin as the blood glucose level is within the normal range.

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
