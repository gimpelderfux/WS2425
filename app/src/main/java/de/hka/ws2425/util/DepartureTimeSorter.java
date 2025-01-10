package de.hka.ws2425.util;

import org.gtfs.reader.model.StopTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

public class DepartureTimeSorter {

    /**
     * Sortiert eine Liste von StopTime-Objekten aufsteigend nach Abfahrtszeit.
     *
     * @param stopTimes Eine Liste von StopTime-Objekten.
     */
    public static void sortStopTimesByDepartureTime(List<StopTime> stopTimes) {
        stopTimes.sort(new StopTimeDepartureTimeComparator());
    }

    /**
     * Ein Comparator, der StopTime-Objekte nach Abfahrtszeit vergleicht.
     */
    private static class StopTimeDepartureTimeComparator implements Comparator<StopTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        private static final DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("HH:mm");

        @Override
        public int compare(StopTime stopTime1, StopTime stopTime2) {
            String time1 = stopTime1.getDepartureTime();
            String time2 = stopTime2.getDepartureTime();

            if (time1 == null && time2 == null) return 0;
            if (time1 == null) return -1;
            if (time2 == null) return 1;

            try {
                LocalTime localTime1 = parseTime(time1);
                LocalTime localTime2 = parseTime(time2);
                return localTime1.compareTo(localTime2);
            } catch (DateTimeParseException e) {
                // Bei fehlerhaften Formaten, behalte die ursprüngliche Reihenfolge bei
                return 0;
            }
        }

        private LocalTime parseTime(String time) {
            try {
                return LocalTime.parse(time, formatter);
            } catch (DateTimeParseException e) {
                return LocalTime.parse(time, shortFormatter);
            }
        }
    }
}