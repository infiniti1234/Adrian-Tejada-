package edu.fiu.cop3337.calendar.model;

/**
 * CalendarDate represents one calendar date (month, day, year).
 * No Java date/time libraries are used.
 */
public class CalendarDate {
    private int month;
    private int day;
    private int year;

    /**
     * Constructs a valid date.
     *
     * @throws IllegalArgumentException if month/day/year is not a real date
     */
    public CalendarDate(int month, int day, int year) {
        if (!isValidDate(month, day, year)) {
            throw new IllegalArgumentException("invalid date " + month + "/" + day + "/" + year);
        }
        this.month = month;
        this.day = day;
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getYear() {
        return year;
    }

    /** Changes the month only if the resulting date is valid. */
    public void setMonth(int month) {
        if (isValidDate(month, this.day, this.year)) {
            this.month = month;
        }
    }

    /** Changes the day only if the resulting date is valid. */
    public void setDay(int day) {
        if (isValidDate(this.month, day, this.year)) {
            this.day = day;
        }
    }

    /** Changes the year only if the resulting date is valid. */
    public void setYear(int year) {
        if (isValidDate(this.month, this.day, year)) {
            this.year = year;
        }
    }

    public boolean isLeapYear() {
        return isLeapYear(year);
    }

    public int daysInMonth() {
        return daysInMonth(month, year);
    }

    public boolean isValidDate() {
        return isValidDate(month, day, year);
    }

    /** True when this date and other have the same month, day, and year. */
    public boolean sameDay(CalendarDate other) {
        return other != null && month == other.month && day == other.day && year == other.year;
    }

    /**
     * Numeric chronological comparison: year, then month, then day.
     * Returns a negative number, zero, or a positive number.
     */
    public int compareChronologically(CalendarDate other) {
        if (year != other.year) {
            return Integer.compare(year, other.year);
        }
        if (month != other.month) {
            return Integer.compare(month, other.month);
        }
        return Integer.compare(day, other.day);
    }

    public static boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true;
        } else if (year % 100 == 0) {
            return false;
        } else {
            return year % 4 == 0;
        }
    }

    /** Days in a month, or 0 for an invalid month/year. */
    public static int daysInMonth(int month, int year) {
        if (year <= 0) {
            return 0;
        }

        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return isLeapYear(year) ? 29 : 28;
            default:
                return 0;
        }
    }

    public static boolean isValidDate(int month, int day, int year) {
        if (year <= 0 || month < 1 || month > 12) {
            return false;
        }
        return day >= 1 && day <= daysInMonth(month, year);
    }

    /**
     * Day of week for a valid date using Zeller's Congruence.
     * 0 = Sunday ... 6 = Saturday.
     */
    public static int dayOfWeek(int month, int day, int year) {
        int m = month;
        int y = year;
        if (m < 3) {
            m = m + 12;
            y = y - 1;
        }

        int k = y % 100;
        int j = y / 100;

        int h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7;

        return (h + 6) % 7;
    }

    /** MM/DD/YYYY, for example 03/01/2026. */
    @Override
    public String toString() {
        return pad(month, 2) + "/" + pad(day, 2) + "/" + pad(year, 4);
    }

    private static String pad(int value, int width) {
        String text = "" + value;
        while (text.length() < width) {
            text = "0" + text;
        }
        return text;
    }
}
