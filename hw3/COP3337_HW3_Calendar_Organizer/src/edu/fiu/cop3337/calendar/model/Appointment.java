package edu.fiu.cop3337.calendar.model;

/**
 * An Appointment IS-A CalendarEvent with the person it is with.
 */
public class Appointment extends CalendarEvent {
    private String withWhom;

    /**
     * Builds the CalendarEvent part with super(...), then sets withWhom.
     * Throws IllegalArgumentException if withWhom is blank.
     */
    public Appointment(String title, CalendarDate date, String startTime,
                       int durationMinutes, String owner, String location,
                       String withWhom) {
        super(title, date, startTime, durationMinutes, owner, location);
        if (isBlank(withWhom)) {
            throw new IllegalArgumentException("withWhom must not be blank");
        }
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

    @Override
    public String toCSV() {
        return appointmentFields(", ");
    }

    @Override
    public String toTSV() {
        return appointmentFields("\t");
    }

    /** Common event data plus withWhom. */
    private String appointmentFields(String delimiter) {
        return commonFields(delimiter) + delimiter + getWithWhom();
    }
}
