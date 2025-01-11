package de.hka.ws2425.ui.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.hka.ws2425.utils.CalendarDate;
import de.hka.ws2425.utils.GtfsData;
import de.hka.ws2425.utils.StopTimes;
import de.hka.ws2425.utils.Stops;

public class MapUtils {

    public static void addStopsToMap(MapView mapView, List<Stops> stopsList, MarkerClickListener listener) {
        for (Stops stop : stopsList) {
            if (stop.getId().endsWith("_Parent")) {
                continue;
            }
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(stop.getLatitude(), stop.getLongitude()));
            marker.setTitle(stop.getName());
            marker.setSubDescription("ID: " + stop.getId());
            marker.setOnMarkerClickListener((m, map) -> {
                listener.onMarkerClick(m, stop);
                return true;
            });
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    public static List<String> getDeparturesForStop(GtfsData gtfsData, String stopId) {
        List<String> departures = new ArrayList<>();

        // 1. Ermittle den aktuellen Betriebstag (im Format YYYYMMDD)
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // 2. Finde alle gültigen Service-IDs für den aktuellen Tag
        Set<String> validServiceIds = new HashSet<>();
        for (CalendarDate calendarDate : gtfsData.getCalendarDates()) {
            if (String.valueOf(calendarDate.getDate()).equals(currentDate)) {
                validServiceIds.add(calendarDate.getServiceId());
            }
        }

        // 3. Überprüfe, ob es gültige Service-IDs gibt
        if (validServiceIds.isEmpty()) {
            departures.add("An diesem Tag gibt es hier keine Fahrten.");
            return departures;
        }

        // 4. Finde alle Stop-Times für die gültigen Service-IDs
        boolean hasDeparturesForDay = false;
        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getStopId().equals(stopId)) {
                String tripId = stopTime.getTripId();

                // Überprüfe, ob die Trip-ID zu einer gültigen Service-ID gehört
                boolean isValidTrip = gtfsData.getTrips().stream()
                        .anyMatch(trip -> trip.getTripId().equals(tripId) && validServiceIds.contains(trip.getServiceId()));

                if (isValidTrip) {
                    hasDeparturesForDay = true;
                    String departure = "Abfahrt um " + stopTime.getDepartureTime() + " - Trip: " + tripId;
                    departures.add(departure);
                }
            }
        }

        // 5. Keine gültigen Abfahrten für den Tag
        if (!hasDeparturesForDay) {
            departures.add("An diesem Tag gibt es hier keine Fahrten.");
        } else if (departures.isEmpty()) {
            departures.add("Keine Abfahrten verfügbar.");
        }

        return departures;
    }

    public interface MarkerClickListener {
        void onMarkerClick(Marker marker, Stops stop);
    }

    public static List<String> getTripDetails(GtfsData gtfsData, String tripId) {
        List<String> tripDetails = new ArrayList<>();

        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getTripId().equals(tripId)) {
                String detail = "Haltestelle: " + stopTime.getStopId() +
                        "\nAnkunft: " + stopTime.getArrivalTime() +
                        "\nAbfahrt: " + stopTime.getDepartureTime();
                tripDetails.add(detail);
            }
        }

        if (tripDetails.isEmpty()) {
            tripDetails.add("Keine Daten für diese Fahrt verfügbar.");
        }

        return tripDetails;
    }
}
