package edu.fiu.cop3337.calendar.model;

/** APPOINTMENT: common fields + withWhom. */
public class Appointment extends CalendarEvent {
    private String withWhom;

    public Appointment(String title, CalendarDate date, String startTime, int durationMinutes,
                       String owner, String location, String withWhom) {
        super(title, date, startTime, durationMinutes, owner, location);
        this.withWhom = requireText(withWhom, "withWhom");
    }

    public Appointment(String title, CalendarDate date, String startTime, int durationMinutes,
                       String withWhom) {
        this(title, date, startTime, durationMinutes, DEFAULT_OWNER, DEFAULT_LOCATION, withWhom);
    }

    public String getWithWhom() {
        return withWhom;
    }

    public void setWithWhom(String withWhom) {
        if (isValidText(withWhom)) {
            this.withWhom = withWhom.trim();
        }
    }

    @Override
    public String getType() {
        return "APPOINTMENT";
    }

    @Override
    public String toCSV() {
        return serialize(", ");
    }

    @Override
    public String toTSV() {
        return serialize("\t");
    }

    private String serialize(String delimiter) {
        return commonFields(delimiter) + delimiter + getWithWhom();
    }
}
