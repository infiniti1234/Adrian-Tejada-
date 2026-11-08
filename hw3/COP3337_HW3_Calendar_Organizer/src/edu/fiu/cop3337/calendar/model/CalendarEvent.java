package edu.fiu.cop3337.calendar.model;

/**
 * CalendarEvent represents one basic calendar item.
 *
 * In HW2 this is the (concrete) superclass of Meeting, Appointment, and Task.
 * It holds the state that every calendar item shares. Subclasses inherit these
 * fields and methods instead of redeclaring them.
 */
public class CalendarEvent {
    private String title;
    private CalendarDate date;
    private String startTime;
    private int durationMinutes;
    private String owner;
    private String location;

    /**
     * Full constructor.
     *
     * CalendarApp validates user input before creating the object, so this
     * constructor may assume its arguments satisfy the HW rules.
     */
    public CalendarEvent(String title, CalendarDate date, String startTime,
                         int durationMinutes, String owner, String location) {
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.owner = owner;
        this.location = location;
    }

    /**
     * Shorter overloaded constructor (constructor overloading).
     * Uses this(...) to reuse the full constructor with default owner/location.
     */
    public CalendarEvent(String title, CalendarDate date, String startTime,
                         int durationMinutes) {
        this(title, date, startTime, durationMinutes, "Unknown", "Unspecified");
    }

    public String getTitle() {
        return title;
    }

    public CalendarDate getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getOwner() {
        return owner;
    }

    public String getLocation() {
        return location;
    }

    /** Update only for nonblank input; otherwise leave unchanged. */
    public void setTitle(String title) {
        if (!isBlank(title)) {
            this.title = title;
        }
    }

    /** Update only for a non-null, valid date; otherwise leave unchanged. */
    public void setDate(CalendarDate date) {
        if (date != null && date.isValidDate()) {
            this.date = date;
        }
    }

    /** Update only when startTime satisfies isValidStartTime(...). */
    public void setStartTime(String startTime) {
        if (isValidStartTime(startTime)) {
            this.startTime = startTime;
        }
    }

    /** Update only when durationMinutes > 0; otherwise leave unchanged. */
    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes > 0) {
            this.durationMinutes = durationMinutes;
        }
    }

    /** Update only for nonblank input; otherwise leave unchanged. */
    public void setOwner(String owner) {
        if (!isBlank(owner)) {
            this.owner = owner;
        }
    }

    /** Update only for nonblank input; otherwise leave unchanged. */
    public void setLocation(String location) {
        if (!isBlank(location)) {
            this.location = location;
        }
    }

    /**
     * Returns true for null or empty-after-trimming strings.
     *
     * Kept inside the model package so the model classes do not depend on
     * CalendarApp. It has package access, so the subclasses (same package)
     * can reuse it in their setters.
     */
    static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    /**
     * Validates the exact time format "hh:mm am" or "hh:mm pm".
     * - hh is exactly two digits from 01 through 12
     * - mm is exactly two digits from 00 through 59
     * - exactly one space appears before am/pm
     * - am/pm is case-insensitive
     */
    public static boolean isValidStartTime(String startTime) {
        // Expected layout (8 characters): h h : m m _ a m
        //                      index:    0 1 2 3 4 5 6 7
        if (startTime == null || startTime.length() != 8) {
            return false;
        }

        char h1 = startTime.charAt(0);
        char h2 = startTime.charAt(1);
        char m1 = startTime.charAt(3);
        char m2 = startTime.charAt(4);

        if (!Character.isDigit(h1) || !Character.isDigit(h2)
                || startTime.charAt(2) != ':'
                || !Character.isDigit(m1) || !Character.isDigit(m2)
                || startTime.charAt(5) != ' ') {
            return false;
        }

        int hour = (h1 - '0') * 10 + (h2 - '0');
        int minute = (m1 - '0') * 10 + (m2 - '0');
        if (hour < 1 || hour > 12 || minute > 59) {
            return false;
        }

        String period = startTime.substring(6);
        return period.equalsIgnoreCase("am") || period.equalsIgnoreCase("pm");
    }

    /** Returns true when month, day, and year match otherDate. */
    public boolean occursOn(CalendarDate otherDate) {
        if (otherDate == null || date == null) {
            return false;
        }
        return date.getMonth() == otherDate.getMonth()
                && date.getDay() == otherDate.getDay()
                && date.getYear() == otherDate.getYear();
    }

    /**
     * Overloaded occursOn (method overloading): same name, different parameters.
     * Reuses occursOn(CalendarDate) instead of repeating the comparison.
     */
    public boolean occursOn(int month, int day, int year) {
        if (!CalendarDate.isValidDate(month, day, year)) {
            return false;
        }
        return occursOn(new CalendarDate(month, day, year));
    }

    /** Case-insensitive owner comparison. */
    public boolean isOwnedBy(String ownerName) {
        if (ownerName == null) {
            return false;
        }
        return owner.equalsIgnoreCase(ownerName.trim());
    }

    /**
     * Returns this item's type label. Each subclass overrides this, and Java
     * picks the right version at runtime (dynamic binding).
     */
    public String getType() {
        return "EVENT";
    }

    /**
     * Output format:
     * TYPE, title, date, startTime, durationMinutes, owner, location
     *
     * getType() is dynamically bound, so a subclass that calls super.toString()
     * gets its own type label (MEETING, TASK, ...) at the front automatically.
     */
    @Override
    public String toString() {
        return getType() + ", " + title + ", " + date + ", " + startTime + ", "
                + durationMinutes + ", " + owner + ", " + location;
    }
}
