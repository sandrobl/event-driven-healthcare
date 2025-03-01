package com.eventdriven.healthcare.patientcheckin.model;


import java.io.Serializable;


public class Gaze implements Serializable {

    private static final long serialVersionUID = 1L;

    int eventID;
    long timestamp;
    int xPosition; // position of the gaze within the x-coordinate of the screen
    int yPosition; // position of the gaze within the y-coordinate of the screen
    int pupilSize; // size of the eye pupil as captured by the eye-tracker


    public Gaze() { }

    public Gaze(int eventID, long timestamp, int xPosition, int yPosition, int pupilSize) {
        this.eventID = eventID;
        this.timestamp = timestamp;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.pupilSize = pupilSize;
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

    public int getPupilSize() {
        return pupilSize;
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

    public void setPupilSize(int pupilSize) {
        this.pupilSize = pupilSize;
    }

    @Override
    public String toString()
    {
        return "eventID: "+eventID+", " +
                "timestamp: "+timestamp+", " +
                "xPosition: "+xPosition+", " +
                "yPosition: "+yPosition+", " +
                "pupilSize: "+pupilSize+", ";
    }

}