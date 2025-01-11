package de.hka.ws2425.utils;

import java.io.Serializable;

public class Calendar implements Serializable {
    private int startDate;
    private int endDate;
    private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    public Calendar(int startDate, int endDate, boolean monday, boolean tuesday, boolean wednesday,
                    boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    public int getStartDate() {
        return startDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public boolean isMonday() {
        return monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public boolean isSunday() {
        return sunday;
    }
}
