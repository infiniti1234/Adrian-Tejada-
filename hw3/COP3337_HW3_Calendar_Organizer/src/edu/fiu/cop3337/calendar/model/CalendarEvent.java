package edu.fiu.cop3337.calendar.model;

/**
 * CalendarEvent is the abstract superclass of every calendar item.
 *
 * It holds the state that every calendar item shares. Subclasses (BasicEvent,
 * Meeting, OnlineMeeting, Appointment, Task) inherit these fields and methods
 * instead of redeclaring them. Because it is abstract, a plain CalendarEvent
 * can no longer be created; an ordinary EVENT record is a BasicEvent.
 *
 * Events are ordered by date, then start time (earliest first), then duration
 * (longest first), then title (ignoring case).
 */
public abstract class CalendarEvent implements CalendarRecord, Comparable<CalendarEvent> {
    private String title;
    private CalendarDate date;
    private String startTime;
    private int durationMinutes;
    private String owner;
    private String location;

    /**
     * Full constructor.
     *
     * CalendarApp validates user input before creating the object. The
     * constructor still protects the object: if any argument breaks a rule it
     * throws IllegalArgumentException, so an invalid event is never built.
     */
    public CalendarEvent(String title, CalendarDate date, String startTime,
                         int durationMinutes, String owner, String location) {
        if (isBlank(title)) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (date == null || !date.isValidDate()) {
            throw new IllegalArgumentException("date must be a valid date");
        }
        if (!isValidStartTime(startTime)) {
            throw new IllegalArgumentException("startTime must use the format hh:mm am/pm");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be greater than 0");
        }
        if (isBlank(owner)) {
            throw new IllegalArgumentException("owner must not be blank");
        }
        if (isBlank(location)) {
            throw new IllegalArgumentException("location must not be blank");
        }

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
     * Converts a valid "hh:mm am/pm" time into minutes after midnight, so times
     * can be compared as numbers. 12:00 am is 0 and 12:00 pm is 720.
     */
    public static int minutesAfterMidnight(String startTime) {
        int hour = Integer.parseInt(startTime.substring(0, 2));
        int minute = Integer.parseInt(startTime.substring(3, 5));
        boolean isPm = startTime.substring(6).equalsIgnoreCase("pm");

        int minutes = (hour % 12) * 60 + minute;
        if (isPm) {
            minutes = minutes + 12 * 60;
        }
        return minutes;
    }

    /**
     * Orders events by date, then start time (earliest first), then duration
     * (longest first), then title ignoring case. Returns 0 when all of these tie.
     * Dates and times are compared as numbers, not as formatted strings.
     */
    @Override
    public int compareTo(CalendarEvent other) {
        int result = Integer.compare(getDate().getYear(), other.getDate().getYear());
        if (result == 0) {
            result = Integer.compare(getDate().getMonth(), other.getDate().getMonth());
        }
        if (result == 0) {
            result = Integer.compare(getDate().getDay(), other.getDate().getDay());
        }
        if (result == 0) {
            result = Integer.compare(minutesAfterMidnight(getStartTime()),
                                     minutesAfterMidnight(other.getStartTime()));
        }
        if (result == 0) {
            // Arguments are swapped so the longer duration comes first.
            result = Integer.compare(other.getDurationMinutes(), getDurationMinutes());
        }
        if (result == 0) {
            result = getTitle().compareToIgnoreCase(other.getTitle());
        }
        return result;
    }

    /**
     * Returns this item's type label (EVENT, MEETING, ONLINE, APPOINTMENT, or
     * TASK). Each concrete subclass supplies it, and Java picks the right
     * version at runtime (dynamic binding).
     */
    public abstract String getType();

    /** Comma-separated record, for example: TASK, Submit Draft, 11/02/2026, ... */
    @Override
    public abstract String toCSV();

    /** Tab-separated record with the same fields as toCSV(), for files. */
    @Override
    public abstract String toTSV();

    /**
     * The seven fields every record starts with, joined by delimiter:
     * type, title, date, startTime, durationMinutes, owner, location.
     * Built from getters; it never calls toString().
     */
    protected String commonFields(String delimiter) {
        return getType() + delimiter + getTitle() + delimiter + getDate()
                + delimiter + getStartTime() + delimiter + getDurationMinutes()
                + delimiter + getOwner() + delimiter + getLocation();
    }

    /** Printing an event shows its comma-separated record. */
    @Override
    public String toString() {
        return toCSV();
    }
}
