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
     * Meeting-specific fields. CalendarApp validates input before calling this.
     */
    public Meeting(String title, CalendarDate date, String startTime,
                   int durationMinutes, String owner, String location,
                   int invitees, String agenda) {
        super(title, date, startTime, durationMinutes, owner, location);
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

    /**
     * Common event data (from the superclass) plus invitees and agenda.
     * super.toString() calls getType(), which is dynamically bound to this
     * class's version, so the line starts with MEETING.
     */
    @Override
    public String toString() {
        return super.toString() + ", " + getInvitees() + ", " + getAgenda();
    }
}
