package edu.fiu.cop3337.calendar.model;

/**
 * A Meeting IS-A CalendarEvent with a number of invitees and an agenda.
 * The common event fields are inherited, not redeclared.
 */
public class Meeting extends CalendarEvent {
    private int invitees;
    private String agenda;

    /**
     * Builds the CalendarEvent part with super(...) first, then the
     * Meeting-specific fields. Throws IllegalArgumentException if invitees is
     * negative or agenda is blank.
     */
    public Meeting(String title, CalendarDate date, String startTime,
                   int durationMinutes, String owner, String location,
                   int invitees, String agenda) {
        super(title, date, startTime, durationMinutes, owner, location);
        if (invitees < 0) {
            throw new IllegalArgumentException("invitees must be 0 or more");
        }
        if (isBlank(agenda)) {
            throw new IllegalArgumentException("agenda must not be blank");
        }
        this.invitees = invitees;
        this.agenda = agenda;
    }

    public int getInvitees() {
        return invitees;
    }

    public String getAgenda() {
        return agenda;
    }

    /** Update only when invitees >= 0; otherwise leave unchanged. */
    public void setInvitees(int invitees) {
        if (invitees >= 0) {
            this.invitees = invitees;
        }
    }

    /** Update only for a nonblank agenda; otherwise leave unchanged. */
    public void setAgenda(String agenda) {
        if (!isBlank(agenda)) {
            this.agenda = agenda;
        }
    }

    @Override
    public String getType() {
        return "MEETING";
    }

    @Override
    public String toCSV() {
        return meetingFields(", ");
    }

    @Override
    public String toTSV() {
        return meetingFields("\t");
    }

    /**
     * Common event data plus invitees and agenda. getType() inside
     * commonFields(...) is dynamically bound, so an OnlineMeeting that reuses
     * this still starts with ONLINE.
     */
    protected String meetingFields(String delimiter) {
        return commonFields(delimiter) + delimiter + getInvitees()
                + delimiter + getAgenda();
    }
}
