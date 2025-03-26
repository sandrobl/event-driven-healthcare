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

It is possible to set the Insulin Calculator REST into slow mode, so it will be unreachable. To do that start the service and then enter S into the console so "slow" mode will be activated.

## Scenarios 

### Required 
Need to be done before any scenario. 

1) Run the only docker compose file 
2) Start all service in intelij or verify that the services are running. 
3) Run the lab-simulation 
    ```bash
    python3 simulate-lab.py
        --broker ftsim.weber.ics.unisg.ch \
        --port 1883 \
        --username ftsim \
        --password unisg \
        --file logs/only_nfc_events.txt
    ```
4) Access the patient guidance webapp via http://localhost:8080 you now should see new patients registred.

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

In this example it will be 1.4866666 ml if you are in a test environment you can create an mqtt event with that exact number using the lab-simulation service. 

Change the content of the **scale_events.txt** so the weight is exactly the same dose that is required. 

There is one case that you have the wrong dose drawn into the syringe. This will prompt you an information telling you either that you have too much in the syringe or too little. And in this case you have to adjust the dose so it is correct to what you need to fill. 

The mqtt scale event can be mocked using this command.

    ```bash
    python3 simulate-lab.py
        --broker ftsim.weber.ics.unisg.ch \
        --port 1883 \
        --username ftsim \
        --password unisg \
        --file logs/scale_events.txt
    ```

After this you will be prompted to administer the injection. Click on the button "Confirm injection". This will unreseve the scale and be the end of the workflow. 

