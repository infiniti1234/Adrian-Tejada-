package edu.fiu.cop3337.calendar.model;

/**
 * An Appointment IS-A CalendarEvent with the person it is with.
 */
public class Appointment extends CalendarEvent {
    private String withWhom;

    /** Builds the CalendarEvent part with super(...), then sets withWhom. */
    public Appointment(String title, CalendarDate date, String startTime,
                       int durationMinutes, String owner, String location,
                       String withWhom) {
        super(title, date, startTime, durationMinutes, owner, location);
        this.withWhom = withWhom;
    }

    public String getWithWhom() {
        return withWhom;
    }

    /** Update only for a nonblank value; otherwise leave unchanged. */
    public void setWithWhom(String withWhom) {
        if (!isBlank(withWhom)) {
            this.withWhom = withWhom;
        }
    }

    @Override
    public String getType() {
        return "APPOINTMENT";
    }

    /** Common event data plus withWhom. */
    @Override
    public String toString() {
        return super.toString() + ", " + getWithWhom();
    }
}
