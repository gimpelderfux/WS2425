package de.hka.ws2425.utils;

import java.io.Serializable;
import java.util.List;

public class GtfsData implements Serializable {
    private List<Stops> stops;
    private List<StopTimes> stopTimes;
    private List<Trips> trips;
    private List<Routes> routes;
    private List<CalendarDate> calendarDates;

    public GtfsData(List<Stops> stops, List<StopTimes> stopTimes, List<Trips> trips,
                    List<Routes> routes, List<CalendarDate> calendarDates) {
        this.stops = stops;
        this.stopTimes = stopTimes;
        this.trips = trips;
        this.routes = routes;
        this.calendarDates = calendarDates;
    }

    public List<Stops> getStops() {
        return stops;
    }

    public List<StopTimes> getStopTimes() {
        return stopTimes;
    }

    public List<Trips> getTrips() {
        return trips;
    }

    public List<Routes> getRoutes() {
        return routes;
    }

    public List<CalendarDate> getCalendarDates() {
        return calendarDates;
    }
}
