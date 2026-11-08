package edu.fiu.cop3337.calendar.model;

/**
 * CalendarDate represents one calendar date.
 *
 * Carried forward from HW1 (unchanged logic).
 *
 * HW1 student work:
 * - leap-year logic
 * - days-in-month logic
 * - date validation
 * - day-of-week calculation
 * - formatted output
 * - setter validation
 */
public class CalendarDate {
    private int month;
    private int day;
    private int year;

    /**
     * Constructs a CalendarDate object.
     *
     * IMPORTANT HW1 POLICY:
     * User-entered dates must be validated BEFORE this constructor is called.
     * CalendarApp.parseDate(...) must call CalendarDate.isValidDate(...), and it
     * must return null for invalid input instead of constructing a default date.
     * Therefore, this constructor may assume that month/day/year form a valid date.
     *
     * @param month month number, 1-12
     * @param day day number valid for the given month/year
     * @param year positive year number
     */
    public CalendarDate(int month, int day, int year) {
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

    /**
     * Changes the month only if the resulting date is valid.
     * Otherwise, leave this object unchanged.
     */
    public void setMonth(int month) {
        if (isValidDate(month, this.day, this.year)) {
            this.month = month;
        }
    }

    /**
     * Changes the day only if the resulting date is valid.
     * Otherwise, leave this object unchanged.
     */
    public void setDay(int day) {
        if (isValidDate(this.month, day, this.year)) {
            this.day = day;
        }
    }

    /**
     * Changes the year only if the resulting date is valid.
     * Otherwise, leave this object unchanged.
     */
    public void setYear(int year) {
        if (isValidDate(this.month, this.day, year)) {
            this.year = year;
        }
    }

    /**
     * Checks whether this object's year is a leap year.
     */
    public boolean isLeapYear() {
        return isLeapYear(year);
    }

    /**
     * Returns the number of days in this object's month.
     */
    public int daysInMonth() {
        return daysInMonth(month, year);
    }

    /**
     * Checks whether this object stores a valid date.
     */
    public boolean isValidDate() {
        return isValidDate(month, day, year);
    }

    /**
     * Leap-year rule:
     * - divisible by 400 -> leap year
     * - divisible by 100 but not 400 -> not leap year
     * - divisible by 4 but not 100 -> leap year
     */
    public static boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true;
        } else if (year % 100 == 0) {
            return false;
        } else {
            return year % 4 == 0;
        }
    }

    /**
     * Returns the number of days in a month, or 0 for an invalid month/year.
     * For HW1, a valid year is greater than 0.
     */
    public static int daysInMonth(int month, int year) {
        if (year <= 0 || month < 1 || month > 12) {
            return 0;
        }

        if (month == 2) {
            if (isLeapYear(year)) {
                return 29;
            }
            return 28;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else {
            return 31;
        }
    }

    /**
     * Returns true only when year > 0, month is 1-12, and day exists in
     * that month/year.
     */
    public static boolean isValidDate(int month, int day, int year) {
        if (year <= 0 || month < 1 || month > 12) {
            return false;
        }
        return day >= 1 && day <= daysInMonth(month, year);
    }

    /**
     * Calculates the day of week for a VALID date.
     *
     * Return value:
     * 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday,
     * 4 = Thursday, 5 = Friday, 6 = Saturday
     *
     * You may use Zeller's Congruence or another correct mathematical formula.
     * Do not use Java date/calendar libraries.
     */
    public static int dayOfWeek(int month, int day, int year) {
        // Zeller's Congruence treats January and February as months 13 and 14
        // of the previous year.
        int m = month;
        int y = year;
        if (m < 3) {
            m = m + 12;
            y = y - 1;
        }

        int k = y % 100;   // year within the century
        int j = y / 100;   // zero-based century

        // h: 0 = Saturday, 1 = Sunday, ..., 6 = Friday
        int h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7;

        // Shift so that 0 = Sunday, 1 = Monday, ..., 6 = Saturday
        return (h + 6) % 7;
    }

    /**
     * Returns the date in MM/DD/YYYY format, for example 03/01/2026.
     */
    @Override
    public String toString() {
        return String.format("%02d/%02d/%04d", month, day, year);
    }
}
