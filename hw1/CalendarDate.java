/**
 * CalendarDate stores a month, day, and year.
 *
 * CalendarApp.parseDate(...) must validate user input with isValidDate(...)
 * before calling the constructor. Invalid input returns null there; no
 * default date is ever substituted.
 */
public class CalendarDate {
    private int month;
    private int day;
    private int year;

    /**
     * Constructs a CalendarDate. Callers must validate the arguments with
     * isValidDate(...) first.
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

    /** Update only if the resulting date is valid; otherwise leave unchanged. */
    public void setMonth(int month) {
        if (isValidDate(month, this.day, this.year)) {
            this.month = month;
        }
    }

    /** Update only if the resulting date is valid; otherwise leave unchanged. */
    public void setDay(int day) {
        if (isValidDate(this.month, day, this.year)) {
            this.day = day;
        }
    }

    /** Update only if the resulting date is valid; otherwise leave unchanged. */
    public void setYear(int year) {
        if (isValidDate(this.month, this.day, year)) {
            this.year = year;
        }
    }

    /**
     * Leap-year rule:
     * divisible by 400 -> leap; divisible by 100 but not 400 -> not leap;
     * otherwise divisible by 4 -> leap.
     */
    public static boolean isLeapYear(int year) {
        if (year % 400 == 0) {
            return true;
        }
        if (year % 100 == 0) {
            return false;
        }
        return year % 4 == 0;
    }

    /** Returns the number of days in month/year, or 0 if month is invalid. */
    public static int daysInMonth(int month, int year) {
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

    /** Year > 0, month 1-12, and the day must exist in that month/year. */
    public static boolean isValidDate(int month, int day, int year) {
        if (year <= 0 || month < 1 || month > 12) {
            return false;
        }
        return day >= 1 && day <= daysInMonth(month, year);
    }

    /** Returns true when this object's fields form a valid date. */
    public boolean isValid() {
        return isValidDate(month, day, year);
    }

    /**
     * Returns 0=Sunday, 1=Monday, ..., 6=Saturday using Zeller's Congruence
     * (Gregorian calendar).
     */
    public static int dayOfWeek(int month, int day, int year) {
        int m = month;
        int y = year;
        // Zeller treats January and February as months 13 and 14 of the prior year.
        if (m < 3) {
            m += 12;
            y -= 1;
        }
        int k = y % 100;
        int j = y / 100;
        // h: 0=Saturday, 1=Sunday, ..., 6=Friday
        int h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7;
        // Shift so that 0=Sunday.
        return (h + 6) % 7;
    }

    /** Day of week for this date: 0=Sunday, ..., 6=Saturday. */
    public int dayOfWeek() {
        return dayOfWeek(month, day, year);
    }

    /** Returns true when month, day, and year all match other. */
    public boolean sameDate(CalendarDate other) {
        return other != null && month == other.month && day == other.day
                && year == other.year;
    }

    /** Returns the date in MM/DD/YYYY format, such as 03/01/2026. */
    @Override
    public String toString() {
        return twoDigits(month) + "/" + twoDigits(day) + "/" + fourDigits(year);
    }

    private static String twoDigits(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    private static String fourDigits(int value) {
        String s = String.valueOf(value);
        while (s.length() < 4) {
            s = "0" + s;
        }
        return s;
    }
}
