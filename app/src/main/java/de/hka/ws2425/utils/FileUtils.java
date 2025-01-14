package de.hka.ws2425.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsSimpleDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static boolean copyAssetToInternalStorage(AssetManager assetManager, String assetFileName, File destinationFile) {
        try (InputStream in = assetManager.open(assetFileName);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            Log.d("FileUtils", "Datei erfolgreich in den internen Speicher kopiert.");
            return true;
        } catch (IOException e) {
            Log.e("FileUtils", "Fehler beim Kopieren der Datei: " + e.getMessage());
            return false;
        }
    }

    public static GtfsData loadGtfsData(File gtfsFile) {
        List<Stops> stopsList = new ArrayList<>();
        List<StopTimes> stopTimesList = new ArrayList<>();
        List<Trips> tripsList = new ArrayList<>();
        List<Routes> routesList = new ArrayList<>();
        List<CalendarDate> calendarDatesList = new ArrayList<>();

        try {
            GtfsSimpleDao gtfsSimpleDao = new GtfsSimpleDao();
            GtfsReader gtfsReader = new GtfsReader();
            gtfsReader.setDataAccessObject(gtfsSimpleDao);
            gtfsReader.read(gtfsFile.getAbsolutePath());

            // Stops
            gtfsSimpleDao.getStops().forEach(stop -> {
                try {
                    double latitude = Double.parseDouble(stop.getLatitude());
                    double longitude = Double.parseDouble(stop.getLongitude());
                    stopsList.add(new Stops(stop.getId(), stop.getName(), latitude, longitude));
                } catch (NumberFormatException e) {
                    Log.e("FileUtils", "Fehler beim Konvertieren der Koordinaten: " + e.getMessage());
                }
            });

            // Stop-Times
            gtfsSimpleDao.getStopTimes().forEach(stopTime -> {
                stopTimesList.add(new StopTimes(
                        stopTime.getTripId(),
                        stopTime.getArrivalTime(),
                        stopTime.getDepartureTime(),
                        stopTime.getStopId()
                ));
            });

            // Trips
            gtfsSimpleDao.getTrips().forEach(trip -> {
                tripsList.add(new Trips(trip.getRouteId(), trip.getServiceId(), trip.getTripId(), trip.getHeadsign()));
            });

            // Routes
            gtfsSimpleDao.getRoutes().forEach(route -> {
                routesList.add(new Routes(route.getId(), route.getAgencyId(), route.getShortName(),
                        route.getLongName(), route.getColor()));
            });


            // CalendarDates
            gtfsSimpleDao.getCalendarDates().forEach(calendarDate -> {
                calendarDatesList.add(new CalendarDate(calendarDate.getServiceId(), calendarDate.getDate()));
            });

            Log.d("FileUtils", "GTFS-Daten erfolgreich geladen.");
        } catch (Exception e) {
            Log.e("FileUtils", "Fehler beim Lesen der GTFS-Datei: " + e.getMessage());
        }

        return new GtfsData(stopsList, stopTimesList, tripsList, routesList, calendarDatesList);
    }


}
