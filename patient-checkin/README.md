# Patient Checkin Service

## Overview
This Spring Boot application handles the patient check-in process in the healthcare system. It manages patient registrations, processes check-in events, and communicates with other services within the event-driven architecture.

## Features
- Patient registration and check-in processing
- Event publishing for downstream services
- RESTful API for patient checkin operations

## Getting Started

### Prerequisites
- JDK 21 or higher
- Maven 
- Message broker (Kafka)


### Installation
```bash
# Clone the repository
git clone https://github.com/yourusername/patient-checkin.git

# Navigate to the project directory
cd patient-checkin

# Build the project
./mvnw clean install
```

### Configuration
Configure the application using `application.yml`:

### Database Setup
This service uses an embedded H2 database for development and testing purposes, configured through the `schema.sql` and `data.sql` files.

#### Schema and Data Initialization
The application includes two important SQL files that automatically run during startup:

1. **schema.sql**
   - Creates the database tables and structure
   - Defines the schema for patient records, check-in history, and other relevant entities
   - Establishes relationships and constraints between tables
   - Automatically executed by Spring Boot when the application starts

2. **data.sql**
   - Populates the database with initial seed data
   - Contains predefined patient records for testing and demonstration
   - Includes sample check-in history and reference data
   - Useful for development, testing, and demonstration environments

These files ensure that:
- The application has a consistent database structure across environments
- Developers can quickly start with pre-populated test data
- The service can be demonstrated without manual data entry
- Integration tests have predictable data to work with

To modify the initial data or schema:
1. Edit the respective SQL file
2. Restart the application to apply changes automatically