package de.hka.ws2425.utils;

/**
 * Represents a stop from the GTFS data.
 */
public class Stops {
    private String id;
    private String name;
    private double latitude;
    private double longitude;

    public Stops(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
