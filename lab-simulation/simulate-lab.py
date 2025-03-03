#!/usr/bin/env python3

import argparse
import time
import json
import signal
import sys
import paho.mqtt.client as mqtt

# Global variable to track shutdown signal
running = True


def handle_shutdown(signum, frame):
    """Handles SIGINT (Ctrl+C) and SIGTERM for a graceful shutdown."""
    global running
    print("\nShutdown signal received. Disconnecting from MQTT broker...")
    running = False


def main():
    parser = argparse.ArgumentParser(description="Sendet Logzeilen als MQTT-Nachrichten.")
    parser.add_argument("--broker", type=str, default="localhost",
                        help="Adresse des MQTT Brokers (Standard: localhost)")
    parser.add_argument("--port", type=int, default=1883,
                        help="Port des MQTT Brokers (Standard: 1883)")
    parser.add_argument("--username", type=str, default=None,
                        help="Benutzername für den MQTT Broker (Standard: None)")
    parser.add_argument("--password", type=str, default=None,
                        help="Passwort für den MQTT Broker (Standard: None)")
    parser.add_argument("--delay", type=float, default=0.25,
                        help="Verzögerung (in Sekunden) zwischen den Nachrichten (Standard: 0.25)")
    parser.add_argument("--file", type=str, default="log.txt",
                        help="Pfad zur Logdatei (Standard: log.txt)")

    args = parser.parse_args()

    # MQTT-Client initialisieren
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)

    # Optional: Username/Password setzen
    if args.username and args.password:
        client.username_pw_set(args.username, args.password)

    try:
        client.connect(args.broker, args.port, 60)
    except Exception as e:
        print(f"Fehler beim Verbindungsaufbau: {e}")
        sys.exit(1)

    client.loop_start()

    # Signalhandler für SIGINT (Ctrl+C) und SIGTERM registrieren
    signal.signal(signal.SIGINT, handle_shutdown)
    signal.signal(signal.SIGTERM, handle_shutdown)

    # Logdatei zeilenweise lesen und senden
    with open(args.file, "r", encoding="utf-8") as f:
        for line in f:
            if not running:
                break  # Exit loop if shutdown is triggered

            line = line.strip()
            if not line:
                continue  # Leere Zeilen überspringen

            try:
                data = json.loads(line)
            except json.JSONDecodeError:
                print(f"Überspringe ungültige JSON-Zeile: {line}")
                continue

            location = data.get("location", "unknown_location")
            sensor_type = data.get("type", "unknown_type")
            topic = f"smart-healthcare/{location}/{sensor_type}"

            # Nachricht veröffentlichen
            client.publish(topic, json.dumps(data))
            print(f"Nachricht veröffentlicht: {topic} -> {data}")

            time.sleep(args.delay)

    print("Beende MQTT Client...")
    client.loop_stop()
    client.disconnect()
    print("MQTT Client erfolgreich getrennt. Programm beendet.")


if __name__ == "__main__":
    main()