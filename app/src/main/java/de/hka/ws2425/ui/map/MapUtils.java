package de.hka.ws2425.ui.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import de.hka.ws2425.utils.Trips;

import de.hka.ws2425.R;
import de.hka.ws2425.utils.CalendarDate;
import de.hka.ws2425.utils.GtfsData;
import de.hka.ws2425.utils.StopTimes;
import de.hka.ws2425.utils.Stops;

public class MapUtils {

    private static final double AVERAGE_SPEED_MPS = 5.6; // Durchschnittsgeschwindigkeit in m/s

    public static void addStopsToMap(MapView mapView, List<Stops> stopsList, MarkerClickListener listener) {
        for (Stops stop : stopsList) {
            if (stop.getId().endsWith("_Parent")) {
                continue;
            }

            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(stop.getLatitude(), stop.getLongitude()));
            marker.setTitle(stop.getName());

            // Icon laden
            Drawable icon = mapView.getContext().getResources().getDrawable(R.drawable.ic_bus_stop, null);

            // Icon skalieren
            int iconWidth = 30; // Gewünschte Breite in Pixeln
            int iconHeight = 30; // Gewünschte Höhe in Pixeln
            Bitmap bitmap;
            if (icon instanceof BitmapDrawable) {
                // Wenn es ein BitmapDrawable ist, extrahiere das Bitmap
                bitmap = ((BitmapDrawable) icon).getBitmap();
            } else {
                // Wenn es kein BitmapDrawable ist, erstelle ein Bitmap daraus
                bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
            }

            // Skaliertes Icon erstellen
            Drawable scaledIcon = new BitmapDrawable(mapView.getContext().getResources(), Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, true));

            // Icon setzen
            marker.setIcon(scaledIcon);

            // Klick-Listener hinzufügen
            marker.setOnMarkerClickListener((m, map) -> {
                listener.onMarkerClick(m, stop);
                return true;
            });

            // Marker zur Karte hinzufügen
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate(); // Karte aktualisieren
    }

    public static Map<String, String> getDeparturesForStop(GtfsData gtfsData, String stopId, List<String> displayDepartures) {
        Map<String, String> tripIdMapping = new HashMap<>(); // Map: "Abfahrt um HH:MM:SS - Ziel: XYZ" -> TripId

        // 1. Ermittle den aktuellen Betriebstag (im Format YYYYMMDD)
        String currentDate = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(new java.util.Date());

        // 2. Finde alle gültigen Service-IDs für den aktuellen Tag
        Set<String> validServiceIds = new HashSet<>();
        for (CalendarDate calendarDate : gtfsData.getCalendarDates()) {
            if (String.valueOf(calendarDate.getDate()).equals(currentDate)) {
                validServiceIds.add(calendarDate.getServiceId());
            }
        }

        // 3. Überprüfe, ob es gültige Service-IDs gibt
        if (validServiceIds.isEmpty()) {
            displayDepartures.add("An diesem Tag gibt es hier keine Fahrten.");
            return tripIdMapping;
        }

        // 4. Hole die aktuelle Uhrzeit in Sekunden
        int currentTimeInSeconds = getCurrentTimeInSeconds();
        Log.d("Berechnung", "Aktuelle Zeit in Sekunden: " + currentTimeInSeconds);

        // 5. Finde alle gültigen Stop-Times und ergänze die Endhaltestelle
        List<String[]> sortedDepartures = new ArrayList<>();

        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getStopId().equals(stopId)) {
                String tripId = stopTime.getTripId();

                // Überprüfe, ob die Trip-ID zu einer gültigen Service-ID gehört
                boolean isValidTrip = gtfsData.getTrips().stream()
                        .anyMatch(trip -> trip.getTripId().equals(tripId) && validServiceIds.contains(trip.getServiceId()));

                if (isValidTrip) {
                    // Hole die Endhaltestelle (TripHeadsign) aus der Klasse Trips
                    Trips trip = gtfsData.getTrips().stream()
                            .filter(t -> t.getTripId().equals(tripId))
                            .findFirst()
                            .orElse(null);

                    String tripHeadsign = (trip != null && trip.getTripHeadsign() != null) ? trip.getTripHeadsign() : "Unbekanntes Ziel";

                    // Konvertiere die Abfahrtszeit in Sekunden
                    int departureTimeInSeconds = parseTimeToSeconds(stopTime.getDepartureTime());

                    // Nur Abfahrten berücksichtigen, die ab jetzt vor 2 Stunden gültig sind
                    if (departureTimeInSeconds >= (currentTimeInSeconds-7200)) {
                        sortedDepartures.add(new String[]{
                                String.valueOf(departureTimeInSeconds), // Für Sortierung
                                tripHeadsign,
                                stopTime.getDepartureTime(), // Ursprüngliches Format für die Anzeige
                                tripId // TripId zur späteren Zuordnung
                        });
                    }
                }
            }
        }

        // 6. Sortiere nach Abfahrtszeit
        sortedDepartures.sort((a, b) -> Integer.compare(Integer.parseInt(a[0]), Integer.parseInt(b[0])));

        // 7. Formatiere die Ausgaben
        for (String[] departure : sortedDepartures) {
            String formattedDeparture = String.format("Abfahrt um %s - Ziel: %s", departure[2], departure[1]);
            displayDepartures.add(formattedDeparture);

            // Speichere die Zuordnung "Anzeige-String -> TripId"
            tripIdMapping.put(formattedDeparture, departure[3]); // departure[3] ist die TripId
        }

        // 8. Keine gültigen Abfahrten mehr für den Tag
        if (displayDepartures.isEmpty()) {
            displayDepartures.add("Keine Abfahrten mehr verfügbar.");
        }

        return tripIdMapping; // Rückgabe der Zuordnung
    }



    public interface MarkerClickListener {
        void onMarkerClick(Marker marker, Stops stop);
    }
    public static List<String> getTripDetailsWithDelays(GtfsData gtfsData, String tripId, GeoPoint currentLocation) {
        List<String> tripDetails = new ArrayList<>();
        int currentTimeInSeconds = getCurrentTimeInSeconds();
        Log.d("Berechnung", "Aktuelle Uhrzeit in Sekunden ab Mitternacht: " + currentTimeInSeconds);

        Stops closestStop = null;
        double minDistance = Double.MAX_VALUE;

        // Finde die nächstgelegene Haltestelle
        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getTripId().equals(tripId)) {
                Stops stop = gtfsData.getStops().stream()
                        .filter(s -> s.getId().equals(stopTime.getStopId()))
                        .findFirst()
                        .orElse(null);

                if (stop != null) {
                    double distance = currentLocation.distanceToAsDouble(new GeoPoint(stop.getLatitude(), stop.getLongitude()));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestStop = stop;
                    }
                }
            }
        }

        if (closestStop == null) {
            tripDetails.add("Keine Haltestellen für diese Fahrt gefunden.");
            return tripDetails;
        }

        Log.d("Berechnung", "Nächstgelegene Haltestelle: " + closestStop.getName());

        // Berechne die Zeitabweichung nur für die nächstgelegene Haltestelle
        int plannedDepartureTimeInSecondsForClosestStop = 0;
        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getTripId().equals(tripId) && stopTime.getStopId().equals(closestStop.getId())) {
                plannedDepartureTimeInSecondsForClosestStop = parseTimeToSeconds(stopTime.getDepartureTime());
                Log.d("Berechnung", "Abfahrtszeit in Sekunden für nächste Haltestelle " + closestStop.getName() + ": " + plannedDepartureTimeInSecondsForClosestStop);
                break;
            }
        }
        int timeDelayInSeconds = currentTimeInSeconds - plannedDepartureTimeInSecondsForClosestStop;
        Log.d("Berechnung", "Zeitabweichung für nächste Haltestelle " + closestStop.getName() + ": " + timeDelayInSeconds);

        String delayText;
        if (timeDelayInSeconds > 0) {
            delayText = String.format("Verspätung: %d Minuten", timeDelayInSeconds / 60);
        } else if (timeDelayInSeconds < 0) {
            delayText = String.format("Verfrühung: %d Minuten", Math.abs(timeDelayInSeconds) / 60);
        } else {
            delayText = "Pünktlich";
        }

        // Füge die Verspätung am Anfang der Liste hinzu
        tripDetails.add(delayText);

        // Gib die Haltestellendetails aus
        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getTripId().equals(tripId)) {
                Stops stop = gtfsData.getStops().stream()
                        .filter(s -> s.getId().equals(stopTime.getStopId()))
                        .findFirst()
                        .orElse(null);

                if (stop == null) {
                    Log.d("Berechnung", "Keine Daten für Stop-ID: " + stopTime.getStopId());
                    continue;
                }

                String stopName = stop.getName();
                Log.d("Berechnung", "Haltestelle: " + stopName);

                int plannedDepartureTimeInSeconds = parseTimeToSeconds(stopTime.getDepartureTime());
                int newDepartureTimeInSeconds = plannedDepartureTimeInSeconds + timeDelayInSeconds;
                String newDepartureTimeString = formatTimeFromSeconds(newDepartureTimeInSeconds);

                String detail = String.format(
                        "Haltestelle: %s\n" +
                                "Ankunft: %s\n" +
                                "Abfahrt: %s\n" +
                                "Voraussichtliche Abfahrt: %s",
                        stopName,
                        stopTime.getArrivalTime(),
                        stopTime.getDepartureTime(),
                        newDepartureTimeString
                );
                tripDetails.add(detail);
            }
        }

        if (tripDetails.isEmpty()) {
            tripDetails.add("Keine Daten für diese Fahrt verfügbar.");
        }

        return tripDetails;
    }

    private static String formatTimeFromSeconds(int totalSeconds) {
        int hours = (totalSeconds / 3600) % 24;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Hilfsmethode zum Konvertieren von HH:MM:SS in Sekunden
    private static int parseTimeToSeconds(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60 + Integer.parseInt(parts[2]);
    }

    // Hilfsmethode zur Berechnung der aktuellen Zeit in Sekunden ab Mitternacht
    private static int getCurrentTimeInSeconds() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour * 3600 + minute * 60 + second;
    }

}
