/**
 * CalendarEvent represents one event in the calendar.
 */
public class CalendarEvent {
    private String title;
    private CalendarDate date;
    private String startTime;
    private int durationMinutes;
    private String owner;
    private String location;

    /**
     * Constructs a CalendarEvent object.
     *
     * CalendarApp.parseEvent(...) must validate user input before creating the
     * object. This constructor may assume its arguments satisfy the HW1 rules.
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
        if (date != null && date.isValid()) {
            this.date = date;
        }
    }

    /**
     * Update only when startTime satisfies isValidStartTime(...).
     * Otherwise leave unchanged.
     */
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

    /** Returns true for null, empty, or whitespace-only strings. */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Validates the exact HW1 time format:
     *
     * hh:mm am
     * or
     * hh:mm pm
     *
     * Rules:
     * - hh is exactly two digits from 01 through 12
     * - mm is exactly two digits from 00 through 59
     * - exactly one space appears before am/pm
     * - am/pm is case-insensitive (am, pm, AM, PM are accepted)
     *
     * Examples:
     * 09:30 am -> valid
     * 01:00 PM -> valid
     * 9:30 am  -> invalid
     * 13:00 pm -> invalid
     */
    public static boolean isValidStartTime(String startTime) {
        // Expected layout: h h : m m ' ' a/p m  -> exactly 8 characters
        if (startTime == null || startTime.length() != 8) {
            return false;
        }

        char h1 = startTime.charAt(0);
        char h2 = startTime.charAt(1);
        char colon = startTime.charAt(2);
        char m1 = startTime.charAt(3);
        char m2 = startTime.charAt(4);
        char space = startTime.charAt(5);
        char ap = Character.toLowerCase(startTime.charAt(6));
        char m = Character.toLowerCase(startTime.charAt(7));

        if (!Character.isDigit(h1) || !Character.isDigit(h2)
                || !Character.isDigit(m1) || !Character.isDigit(m2)) {
            return false;
        }
        if (colon != ':' || space != ' ') {
            return false;
        }
        if ((ap != 'a' && ap != 'p') || m != 'm') {
            return false;
        }

        int hour = (h1 - '0') * 10 + (h2 - '0');
        int minute = (m1 - '0') * 10 + (m2 - '0');
        return hour >= 1 && hour <= 12 && minute >= 0 && minute <= 59;
    }

    /** Returns true when month, day, and year match otherDate. */
    public boolean occursOn(CalendarDate otherDate) {
        if (date == null || otherDate == null) {
            return false;
        }
        return date.getMonth() == otherDate.getMonth()
                && date.getDay() == otherDate.getDay()
                && date.getYear() == otherDate.getYear();
    }

    /** Case-insensitive owner comparison. */
    public boolean isOwnedBy(String ownerName) {
        return owner != null && ownerName != null
                && owner.equalsIgnoreCase(ownerName);
    }

    /**
     * Required output format:
     * EVENT, title, date, startTime, durationMinutes, owner, location
     */
    @Override
    public String toString() {
        return "EVENT, " + title + ", " + date + ", " + startTime + ", "
                + durationMinutes + ", " + owner + ", " + location;
    }
}
