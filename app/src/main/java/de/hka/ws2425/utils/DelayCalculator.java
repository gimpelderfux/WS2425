package de.hka.ws2425.utils;

import java.time.LocalTime;

public class TimetableEntry {
    private String busId;
    private String stopId;
    private LocalTime scheduledArrivalTime;

    public TimetableEntry(String busId, String stopId, LocalTime scheduledArrivalTime) {
        this.busId = busId;
        this.stopId = stopId;
        this.scheduledArrivalTime = scheduledArrivalTime;
    }

    public String getBusId() {
        return busId;
    }

    public String getStopId() {
        return stopId;
    }

    public LocalTime getScheduledArrivalTime() {
        return scheduledArrivalTime;
    }
}
