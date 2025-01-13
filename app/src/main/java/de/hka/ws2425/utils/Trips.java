package de.hka.ws2425.utils;

import java.io.Serializable;

public class Trips implements Serializable {
    private String routeId;
    private String serviceId;
    private String tripId;
    private String tripHeadsign;

    public Trips(String routeId, String serviceId, String tripId, String tripHeadsign) {
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.tripHeadsign = tripHeadsign;
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

    public String getTripHeadsign() {
        return tripHeadsign;
    }

}