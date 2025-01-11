package de.hka.ws2425.utils;

import java.io.Serializable;

public class StopTimes implements Serializable {
    private String tripId;
    private String arrivalTime;
    private String departureTime;
    private String stopId;

    public StopTimes(String tripId, String arrivalTime, String departureTime, String stopId) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getStopId() {
        return stopId;
    }
}
