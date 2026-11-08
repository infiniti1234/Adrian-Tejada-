package edu.fiu.cop3337.calendar.model;

/** MEETING: common fields + invitees, agenda. */
public class Meeting extends CalendarEvent {
    private int invitees;
    private String agenda;

    public Meeting(String title, CalendarDate date, String startTime, int durationMinutes,
                   String owner, String location, int invitees, String agenda) {
        super(title, date, startTime, durationMinutes, owner, location);
        if (invitees < 0) {
            throw new IllegalArgumentException("invitees must be 0 or more");
        }
        this.invitees = invitees;
        this.agenda = requireText(agenda, "agenda");
    }

    public Meeting(String title, CalendarDate date, String startTime, int durationMinutes,
                   int invitees, String agenda) {
        this(title, date, startTime, durationMinutes, DEFAULT_OWNER, DEFAULT_LOCATION,
                invitees, agenda);
    }

    public int getInvitees() {
        return invitees;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setInvitees(int invitees) {
        if (invitees >= 0) {
            this.invitees = invitees;
        }
    }

    public void setAgenda(String agenda) {
        if (isValidText(agenda)) {
            this.agenda = agenda.trim();
        }
    }

    @Override
    public String getType() {
        return "MEETING";
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
        return commonFields(delimiter) + delimiter + getInvitees() + delimiter + getAgenda();
    }
}
