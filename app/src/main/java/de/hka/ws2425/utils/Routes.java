package de.hka.ws2425.utils;

import java.io.Serializable;

public class Routes implements Serializable {
    private String id;
    private String agencyId;
    private String shortName;
    private String longName;
    private String color;

    public Routes(String id, String agencyId, String shortName, String longName, String color) {
        this.id = id;
        this.agencyId = agencyId;
        this.shortName = shortName;
        this.longName = longName;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getColor() {
        return color;
    }
}
