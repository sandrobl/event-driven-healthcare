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

# Navigate to the project directory
cd patient-checkin

# Build the project
./mvnw clean install

### Configuration
Configure the application using `application.yml`:

### Database Setup
This service uses an SQLite database, configured through the `DataSourceConfig` class with the database file `patients.db`.

#### Database Configuration
The application is configured to use SQLite with the following setup in `DataSourceConfig.java`:

```java
@Bean
public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.sqlite.JDBC");
    dataSource.setUrl("jdbc:sqlite:patients.db");
    return dataSource;
}
```


#### Schema and Data Initialization
The application uses SQLite for persistent storage:

1. **Database File**
   - The SQLite database is stored in the file `patients.db`
   - Contains tables for patient records, check-in history, and other relevant entities
   - Persists data between application restarts

2. **Schema and Data Management**
   - Database schema is managed through your application code or migration scripts
   - Initial data can be loaded programmatically or through SQL scripts
   - Changes to the database structure should follow proper migration practices

This configuration ensures that:
- The application has consistent data storage across restarts
- Data persists between sessions
- The service can be deployed with minimal external dependencies
- Integration tests can use the same database technology as production

To modify the database configuration:
1. Edit the `DataSourceConfig.java` file
2. Restart the application to apply changes