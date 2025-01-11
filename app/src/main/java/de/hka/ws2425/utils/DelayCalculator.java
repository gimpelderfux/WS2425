package de.hka.ws2425.utils;

import android.location.Location;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DelayCalculator {

    public static final double THRESHOLD_DISTANCE = 100.0; // meters

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371.0; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c * 1000; // Distance in meters
    }

    public static Stops findClosestStop(Location currentLocation, List<Stops> stops) {
        Stops closestStop = null;
        double minDistance = Double.MAX_VALUE;

        for (Stops stop : stops) {
            double distance = calculateDistance(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    stop.getLatitude(),
                    stop.getLongitude()
            );
            if (distance < minDistance) {
                minDistance = distance;
                closestStop = stop;
            }
        }

        return closestStop;
    }

    public static long calculateDelay(Date scheduledTime, Date actualTime) {
        long diffInMillies = actualTime.getTime() - scheduledTime.getTime();
        return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static Date convertToDate(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}