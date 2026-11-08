package edu.fiu.cop3337.calendar.model;

/** An ordinary EVENT record with only the seven common fields. */
public class BasicEvent extends CalendarEvent {

    public BasicEvent(String title, CalendarDate date, String startTime,
                      int durationMinutes, String owner, String location) {
        super(title, date, startTime, durationMinutes, owner, location);
    }

    public BasicEvent(String title, CalendarDate date, String startTime, int durationMinutes) {
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
