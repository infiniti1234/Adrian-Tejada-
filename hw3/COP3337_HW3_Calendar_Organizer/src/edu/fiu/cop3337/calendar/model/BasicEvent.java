package edu.fiu.cop3337.calendar.model;

/**
 * A BasicEvent is an ordinary EVENT record: only the seven common fields.
 * Since CalendarEvent is abstract, EVENT lines are built as BasicEvent objects.
 */
public class BasicEvent extends CalendarEvent {

    /** Full constructor, matching CalendarEvent's full constructor. */
    public BasicEvent(String title, CalendarDate date, String startTime,
                      int durationMinutes, String owner, String location) {
        super(title, date, startTime, durationMinutes, owner, location);
    }

    /** Short constructor, matching CalendarEvent's short constructor. */
    public BasicEvent(String title, CalendarDate date, String startTime,
                      int durationMinutes) {
        super(title, date, startTime, durationMinutes);
    }

    @Override
    public String getType() {
        return "EVENT";
    }

    @Override
    public String toCSV() {
        return commonFields(", ");
    }

    @Override
    public String toTSV() {
        return commonFields("\t");
    }
}
