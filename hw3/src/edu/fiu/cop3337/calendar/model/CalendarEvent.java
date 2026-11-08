package edu.fiu.cop3337.calendar.model;

/**
 * Abstract superclass for every calendar item.
 *
 * Common fields (in serialization order after the type token):
 * title, date, startTime, durationMinutes, owner, location.
 *
 * startTime keeps the HW#2 text representation "hh:mm am" / "hh:mm pm".
 */
public abstract class CalendarEvent implements CalendarRecord, Comparable<CalendarEvent> {
    public static final String DEFAULT_OWNER = "Unassigned";
    public static final String DEFAULT_LOCATION = "TBD";

    private String title;
    private CalendarDate date;
    private String startTime;
    private int durationMinutes;
    private String owner;
    private String location;

    /**
     * Full constructor.
     *
     * @throws IllegalArgumentException if any argument breaks an object invariant
     */
    public CalendarEvent(String title, CalendarDate date, String startTime,
                         int durationMinutes, String owner, String location) {
        this.title = requireText(title, "title");
        if (date == null || !date.isValidDate()) {
            throw new IllegalArgumentException("date must be a valid date");
        }
        this.date = date;
        if (!isValidStartTime(startTime)) {
            throw new IllegalArgumentException("startTime must use hh:mm am/pm");
        }
        this.startTime = startTime;
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive");
        }
        this.durationMinutes = durationMinutes;
        this.owner = requireText(owner, "owner");
        this.location = requireText(location, "location");
    }

    /** Short constructor: owner and location get default values. */
    public CalendarEvent(String title, CalendarDate date, String startTime, int durationMinutes) {
        this(title, date, startTime, durationMinutes, DEFAULT_OWNER, DEFAULT_LOCATION);
    }

    // ----- abstract, polymorphic behavior -----

    /** Type token used in records: EVENT, MEETING, ONLINE, APPOINTMENT, TASK. */
    public abstract String getType();

    @Override
    public abstract String toCSV();

    @Override
    public abstract String toTSV();

    // ----- getters -----

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

    // ----- setters: invalid values leave the object unchanged -----

    public void setTitle(String title) {
        if (isValidText(title)) {
            this.title = title.trim();
        }
    }

    public void setDate(CalendarDate date) {
        if (date != null && date.isValidDate()) {
            this.date = date;
        }
    }

    public void setStartTime(String startTime) {
        if (isValidStartTime(startTime)) {
            this.startTime = startTime;
        }
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes > 0) {
            this.durationMinutes = durationMinutes;
        }
    }

    public void setOwner(String owner) {
        if (isValidText(owner)) {
            this.owner = owner.trim();
        }
    }

    public void setLocation(String location) {
        if (isValidText(location)) {
            this.location = location.trim();
        }
    }

    // ----- shared behavior -----

    /**
     * Validates the HW#2 time format "hh:mm am" or "hh:mm pm":
     * hh is 01-12, mm is 00-59, exactly one space, am/pm in any case.
     */
    public static boolean isValidStartTime(String startTime) {
        if (startTime == null || startTime.length() != 8) {
            return false;
        }

        char h1 = startTime.charAt(0);
        char h2 = startTime.charAt(1);
        char m1 = startTime.charAt(3);
        char m2 = startTime.charAt(4);

        if (!Character.isDigit(h1) || !Character.isDigit(h2)
                || !Character.isDigit(m1) || !Character.isDigit(m2)) {
            return false;
        }
        if (startTime.charAt(2) != ':' || startTime.charAt(5) != ' ') {
            return false;
        }

        String amPm = startTime.substring(6).toLowerCase();
        if (!amPm.equals("am") && !amPm.equals("pm")) {
            return false;
        }

        int hour = (h1 - '0') * 10 + (h2 - '0');
        int minute = (m1 - '0') * 10 + (m2 - '0');
        return hour >= 1 && hour <= 12 && minute >= 0 && minute <= 59;
    }

    /**
     * Converts the stored hh:mm am/pm time to minutes after midnight
     * (12:00 am = 0, 12:00 pm = 720), for numeric comparison.
     */
    public int getStartMinutes() {
        int hour = Integer.parseInt(startTime.substring(0, 2));
        int minute = Integer.parseInt(startTime.substring(3, 5));
        boolean pm = startTime.substring(6).equalsIgnoreCase("pm");
        int hour24 = hour % 12;
        if (pm) {
            hour24 += 12;
        }
        return hour24 * 60 + minute;
    }

    /** True when this item is on otherDate. */
    public boolean occursOn(CalendarDate otherDate) {
        return date.sameDay(otherDate);
    }

    /** Overload: true when this item is on month/day/year. */
    public boolean occursOn(int month, int day, int year) {
        return date.getMonth() == month && date.getDay() == day && date.getYear() == year;
    }

    /** Case-insensitive owner comparison. */
    public boolean isOwnedBy(String ownerName) {
        if (ownerName == null) {
            return false;
        }
        return owner.equalsIgnoreCase(ownerName.trim());
    }

    /**
     * Order: date, then start time (earliest first); for equal start
     * moments, longer duration first; then title ignoring case.
     * All comparisons use numeric values, not formatted strings.
     */
    @Override
    public int compareTo(CalendarEvent other) {
        int result = date.compareChronologically(other.date);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(getStartMinutes(), other.getStartMinutes());
        if (result != 0) {
            return result;
        }
        result = Integer.compare(other.durationMinutes, durationMinutes);
        if (result != 0) {
            return result;
        }
        return title.compareToIgnoreCase(other.title);
    }

    /** Console display uses the comma-separated record. */
    @Override
    public String toString() {
        return toCSV();
    }

    /**
     * Builds the seven common fields from getters, joined by delimiter.
     * Subclasses append their own fields.
     */
    protected String commonFields(String delimiter) {
        return getType() + delimiter + getTitle() + delimiter + getDate() + delimiter
                + getStartTime() + delimiter + getDurationMinutes() + delimiter
                + getOwner() + delimiter + getLocation();
    }

    // ----- text helpers shared by the model classes -----

    /** True for null or empty-after-trimming strings. */
    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    /** True if text contains a comma, tab, or line break (cannot be serialized). */
    public static boolean containsSeparator(String text) {
        return text != null && (text.indexOf(',') >= 0 || text.indexOf('\t') >= 0
                || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0);
    }

    protected static boolean isValidText(String text) {
        return !isBlank(text) && !containsSeparator(text);
    }

    /**
     * Returns trimmed text, or throws if it is blank or contains a
     * separator character.
     */
    protected static String requireText(String text, String fieldName) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (containsSeparator(text)) {
            throw new IllegalArgumentException(fieldName + " must not contain commas, tabs, or line breaks");
        }
        return text.trim();
    }
}
