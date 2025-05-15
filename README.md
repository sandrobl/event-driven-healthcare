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
| stream processor        | [Stream Processor README](/stream-processor/README.md)                 |

It is possible to set the Insulin Calculator REST into slow mode, so it will be unreachable. To do that start the service and then enter S into the console so "slow" mode will be activated.

## LAB Demo

https://github.com/user-attachments/assets/2d91a661-d545-40ba-b4ba-7c038b523e57


## Accessible WEB Frontends / HTTP Endpoints
- *Monitor Dashboard* (http://localhost:7070): A real-time dashboard for the Kafka Streams processor. Browse live views of the enriched patient table, total insulin per patient, windowed insulin sums, and average weight changes over time.

- *Patient Guidance Web App* (http://localhost:8080): The clinician‐facing UI that displays patient data and guide trough the inuslin process. Provides forms for manual data entry.

- *Camunda Orchestrator* (http://localhost:8090): The Camunda BPM cockpit and tasklist. Allows workflow monitoring, user-task management, and process instance inspection for all patient-related business processes. Login: demo/demo

- *Insulin Calculator API* (http://localhost:8095)  
  A REST service endpoint for computing recommended insulin doses. Accepts patient metrics and returns personalized dose suggestions back to the process. Could also simulate a slow mode for testing the process with an slow responding API.

- *Kafdrop* (http://localhost:9000)
  A third party GUI to inspect Kafka Topics and its messages.

## Scenarios 

### Required 
Need to be done before any scenario. 

1) Run the only docker compose file 
2) Start all service in intelij or verify that the services are running. 
3) Run the stream-processor service (with the main function being in the class EventProcessingApp)
4) Run the lab-simulation 
    ```bash
    python3 simulate-lab.py
        --broker ftsim.weber.ics.unisg.ch \
        --port 1883 \
        --username ftsim \
        --password unisg \
        --file logs/only_nfc_events.txt
    ```
5) Access the patient guidance webapp via http://localhost:8080 you now should see new patients registred.

### No insulin required

Enter following information for Patient: **John Doe, NFC-Id = 04DAF28AB45780**
* **Next Meal Carbohydrates:** 500 grams (Unrealistically high)
* **Insulin To Carbohydrate Ratio:** 3 (Very low ratio)
* **Target Blood Glucose Level:** 4 mmol/L
* **Blood Glucose:** 6 mmol/L (Normal)

This patient does not require any insulin as the blood glucose level is within the normal range.

### Insulin required

Enter following information for Patient: **Jane Smith, NFC-Id = 08DAC28BB44223**

* **Next Meal Carbohydrates:** 400 grams (Extremely large meal)
* **Insulin To Carbohydrate Ratio:** 3 (Very low ratio)
* **Target Blood Glucose Level:** 4 mmol/L
* **Blood Glucose:** 50 mmol/L (Dangerously high)

The scale is now reserved for the user. 

The patient now has to weigh the correct amount of insulin for her insulin dose. 

In this example it will be 1 ml if you are in a test environment you can create an mqtt event with that exact number using the lab-simulation service. Or just create one with too much weight to see the process going through the loop. 

Change the content of the **scale_events.txt** so the weight is exactly the same dose that is required. 

There is one case that you have the wrong dose drawn into the syringe. This will prompt you an information telling you either that you have too much in the syringe or too little. And in this case you have to adjust the dose so it is correct to what you need to fill. 

The mqtt scale event can be mocked using this command.

    python3 simulate-lab.py
        --broker ftsim.weber.ics.unisg.ch \
        --port 1883 \
        --username ftsim \
        --password unisg \
        --file logs/scale_events.txt

After this you will be prompted to administer the injection. Click on the button "Confirm injection". This will unreseve the scale and be the end of the workflow. 

