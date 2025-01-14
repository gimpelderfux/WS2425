package de.hka.ws2425.utils;

import java.io.Serializable;

public class CalendarDate implements Serializable {
    private String serviceId;
    private int date;

    public CalendarDate(String serviceId, int date) {
        this.serviceId = serviceId;
        this.date = date;
    }

    public String getServiceId() {
        return serviceId;
    }

    public int getDate() {
        return date;
    }
}
