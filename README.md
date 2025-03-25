# event-driven-healthcare
This is a system for health care workflows. Currently we focused on one to of administer insulin doses as displayed in here. 

![BPMN Process](/camunda-orchestrator/src/main/resources/process.png)

There are 7 services that work in tandem to achieve a solution from detection if insulin is needed to administer the correctly calculated and filled up dose. 

| Module| README Link|
|-------------|-------------|
| Camunda Orchestrator | [Camunda Orchestrator README](camunda-orchestrator/README.md) |
| Insulin Calculator REST     | [Insulin Calculator REST README](/insulin-calculator-rest/README.md)         |
| lab-simulation   | [Lab simulation README](/lab-simulation/README.md)   |
| mqtt kafka bridge       | [MQTT Kafka Bridge README](/mqtt-kafka-bridge/README.md)          |
| patient checkin      | [Patient Checkin README](/patient-checkin/README.md)        |
| patient guidance webapp           | [Patient Guidance WebApp README](/patient-guidance-webapp/README.md)                 |