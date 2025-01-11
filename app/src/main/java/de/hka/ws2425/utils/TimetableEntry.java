package de.hka.ws2425.utils;

public class TimetableEntry {
    private String busId;
    private String stopId;
    private int scheduledArrivalHour;
    private int scheduledArrivalMinute;

    public TimetableEntry(String busId, String stopId, int scheduledArrivalHour, int scheduledArrivalMinute) {
        this.busId = busId;
        this.stopId = stopId;
        this.scheduledArrivalHour = scheduledArrivalHour;
        this.scheduledArrivalMinute = scheduledArrivalMinute;
    }

    public String getBusId() {
        return busId;
    }

    public String getStopId() {
        return stopId;
    }

    public int getScheduledArrivalHour() {
        return scheduledArrivalHour;
    }

    public int getScheduledArrivalMinute() {
        return scheduledArrivalMinute;
    }
}