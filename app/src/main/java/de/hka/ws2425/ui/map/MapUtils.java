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

    public static List<String> getDeparturesForStop(GtfsData gtfsData, String stopId) {
        List<String> departures = new ArrayList<>();

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

    public static List<String> getTripDetailsWithDelays(GtfsData gtfsData, String tripId, GeoPoint currentLocation) {
        List<String> tripDetails = new ArrayList<>();
        double averageSpeedMps = 5.6; // Durchschnittsgeschwindigkeit in m/s

        // Berechne die aktuelle Zeit in Sekunden ab Mitternacht
        int currentTimeInSeconds = getCurrentTimeInSeconds();
        Log.d("Berechnung", "Aktuelle Uhrzeit in Sekunden ab Mitternacht: " + currentTimeInSeconds);

        for (StopTimes stopTime : gtfsData.getStopTimes()) {
            if (stopTime.getTripId().equals(tripId)) {
                // Hole die entsprechende Haltestelle
                Stops stop = gtfsData.getStops().stream()
                        .filter(s -> s.getId().equals(stopTime.getStopId()))
                        .findFirst()
                        .orElse(null);

                if (stop == null) {
                    Log.d("Berechnung", "Keine Daten für Stop-ID: " + stopTime.getStopId());
                    continue; // Überspringen, wenn die Haltestelle nicht gefunden wird
                }

                String stopName = stop.getName();
                GeoPoint stopLocation = new GeoPoint(stop.getLatitude(), stop.getLongitude());
                Log.d("Berechnung", "Stop Location: " + stopLocation.toDoubleString());
                Log.d("Berechnung", "currentLocation: " + currentLocation.toDoubleString());

                // Konvertiere geplante Abfahrtszeit in Sekunden
                int plannedDepartureTimeInSeconds = parseTimeToSeconds(stopTime.getDepartureTime());
                Log.d("Berechnung", "Abfahrtszeit in Sekunden für Stop " + stopName + ": " + plannedDepartureTimeInSeconds);

                // Berechne Zeitabweichung
                int timeDelayInSeconds = currentTimeInSeconds - plannedDepartureTimeInSeconds;
                Log.d("Berechnung", "Zeitabweichung für Stop " + stopName + ": " + timeDelayInSeconds);

                // Berechne Distanzabweichung
                double distanceToStop = currentLocation.distanceToAsDouble(stopLocation); // Distanz in Metern
                Log.d("Berechnung", "Distanz zur Haltestelle " + stopName + ": " + distanceToStop + " Meter");
                int distanceDelayInSeconds = (int) (distanceToStop / averageSpeedMps);
                Log.d("Berechnung", "Distanzabweichung für Stop " + stopName + ": " + distanceDelayInSeconds);

                // Gesamtabweichung berechnen
                int totalDelayInSeconds = Math.abs(timeDelayInSeconds) + distanceDelayInSeconds;
                Log.d("Berechnung", "Gesamtabweichung für Stop " + stopName + ": " + totalDelayInSeconds);

                String delayText = String.format("Abweichung: %d Minuten", totalDelayInSeconds / 60);

                // Detailtext hinzufügen
                String detail = String.format(
                        "Haltestelle: %s\nAnkunft: %s\nAbfahrt: %s\n%s",
                        stopName,
                        stopTime.getArrivalTime(),
                        stopTime.getDepartureTime(),
                        delayText
                );
                tripDetails.add(detail);
            }
        }

        if (tripDetails.isEmpty()) {
            tripDetails.add("Keine Daten für diese Fahrt verfügbar.");
        }

        return tripDetails;
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
