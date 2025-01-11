package de.hka.ws2425.utils;

import android.location.Location;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DelayCalculator {

    public static Stops findClosestStop(Location location, List<Stops> stops) {
        if (location == null || stops == null || stops.isEmpty()) {
            return null;
        }

        Stops closestStop = null;
        float closestDistance = Float.MAX_VALUE;

        for (Stops stop : stops) {
            float[] results = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    stop.getLatitude(), stop.getLongitude(), results);
            float distance = results[0];

            if (distance < closestDistance) {
                closestDistance = distance;
                closestStop = stop;
            }
        }

        return closestStop;
    }

    public static Date convertToDate(String timeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Assuming GTFS times are in UTC
        try {
            return dateFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long calculateDelay(Date scheduledTime, Date actualTime) {
        if (scheduledTime == null || actualTime == null) {
            return 0;
        }
        long delayInMillis = actualTime.getTime() - scheduledTime.getTime();
        return delayInMillis / (60 * 1000); // Convert milliseconds to minutes
    }
}