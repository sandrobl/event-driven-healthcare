# Simulate Lab - MQTT Log Sender

This script reads log files containing JSON-formatted messages and sends them to an MQTT broker at a configurable delay. It simulates a lab environment by publishing real-time sensor data.

## Requirements
- Python 3
- `paho-mqtt` library

Install dependencies if not already installed:
```bash
pip install paho-mqtt
```

## Usage
Run the script with the following command:
```python
python3 simulate-lab.py
    --broker ftsim.weber.ics.unisg.ch \
    --port 1883 \
    --username ftsim \
    --password unisg \
    --file logs/only_nfc_events.txt
```

For adding new patients change the file to **logs/only_nfc_events.txt**.

To feed the system scale data use the file **logs/scale_events.txt** and change the value to the wished values.

### Parameters
- `--broker` : MQTT broker address
- `--port` : MQTT broker port (default: 1883)
- `--username` : MQTT username (if required)
- `--password` : MQTT password (if required)
- `--file` : Path to the log file containing sensor data

## Visualizing MQTT Messages
To monitor and visualize the messages sent to the broker, we recommend using **MQTT Explorer**:
- Download and install **MQTT Explorer**: [https://mqtt-explorer.com/](https://mqtt-explorer.com/)
- Connect to the broker using the provided credentials
- Observe the real-time messages being published

## Notes
- Ensure the MQTT broker is running and accessible.
- Adjust the `--delay` parameter in the script if needed to control message frequency.

Enjoy simulating lab data with MQTT!