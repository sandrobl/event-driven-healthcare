# Patient Guidance WebApp

A web application that provides real-time patient status monitoring and guidance through the process of measuring and administer insulin.

## Prerequisites

- Java JDK 21
- Maven

## Installation

1. Build the project:
   - **Maven:**
     ~~~bash
     mvn clean install
     ~~~

## Configuration
Customize settings in the `src/main/resources/application.yml` file.


## Frontend Resources
This module, will also host a webapp at port 8080. The information about the UI and how it works are found here: **src/resources/static/js**. 

## Running the Application

- **Using Maven:**
  ~~~bash
  mvn spring-boot:run
  ~~~
- **Using the generated jar:**
  ~~~bash
  java -jar target/your-application.jar
  ~~~

## Usage

Access your application at `http://localhost:8080` and adjust the endpoints as needed.
