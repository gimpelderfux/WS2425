package de.hka.ws2425.utils;

import android.location.Location;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        Calendar calendar = Calendar.getInstance();
        String[] timeParts = timeString.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2]);

        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static long calculateDelay(Date scheduledTime, Date actualTime) {
        if (scheduledTime == null || actualTime == null) {
            return 0;
        }
        long delayInMillis = actualTime.getTime() - scheduledTime.getTime();
        return delayInMillis / (60 * 1000); // Convert milliseconds to minutes
    }
}