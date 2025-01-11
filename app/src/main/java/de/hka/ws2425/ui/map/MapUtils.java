package de.hka.ws2425.ui.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

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
        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getStopId().equals(stopId)) {
                String departure = "Abfahrt um " + stopTime.getDepartureTime() + " - Trip: " + stopTime.getTripId();
                departures.add(departure);
            }
        }

        if (departures.isEmpty()) {
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
