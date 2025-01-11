package de.hka.ws2425.utils;

import java.io.Serializable;

public class Trips implements Serializable {
    private String routeId;
    private String serviceId;
    private String tripId;

    public Trips(String routeId, String serviceId, String tripId) {
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.tripId = tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getTripId() {
        return tripId;
    }
}