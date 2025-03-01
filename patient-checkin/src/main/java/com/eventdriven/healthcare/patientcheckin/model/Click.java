package com.eventdriven.healthcare.patientcheckin.model;

import java.io.Serializable;

public class Click implements Serializable {

    private static final long serialVersionUID = 1L;

    int eventID;
    long timestamp;
    int xPosition; // position of the click within the x-coordinate of the screen
    int yPosition; // position of the click within the y-coordinate of the screen
    String clickedElement;

    public Click() {

    }

    public Click(int eventID, long timestamp, int xPosition, int yPosition, String clickedElement) {
        this.eventID = eventID;
        this.timestamp = timestamp;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.clickedElement = clickedElement;
    }

    public int getEventID() {
        return eventID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public String getClickedElement() {
        return clickedElement;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public void setClickedElement(String clickedElement) {
        this.clickedElement = clickedElement;
    }

    @Override
    public String toString()
    {
        return "eventID: "+eventID+", " +
                "timestamp: "+timestamp+", " +
                "xPosition: "+xPosition+", " +
                "yPosition: "+yPosition+", " +
                "clickedElement: "+clickedElement+", ";
    }
}