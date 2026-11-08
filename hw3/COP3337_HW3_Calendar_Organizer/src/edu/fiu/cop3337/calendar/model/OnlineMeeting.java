package edu.fiu.cop3337.calendar.model;

/**
 * An OnlineMeeting IS-A Meeting, and therefore also IS-A CalendarEvent
 * (multilevel inheritance). It adds a platform and a meeting link.
 */
public class OnlineMeeting extends Meeting {
    private String platform;
    private String meetingLink;

    /**
     * Calls the Meeting constructor with super(...), which in turn calls the
     * CalendarEvent constructor. Then sets the online-specific fields.
     * Throws IllegalArgumentException if platform or meetingLink is blank.
     */
    public OnlineMeeting(String title, CalendarDate date, String startTime,
                         int durationMinutes, String owner, String location,
                         int invitees, String agenda,
                         String platform, String meetingLink) {
        super(title, date, startTime, durationMinutes, owner, location,
              invitees, agenda);
        if (isBlank(platform)) {
            throw new IllegalArgumentException("platform must not be blank");
        }
        if (isBlank(meetingLink)) {
            throw new IllegalArgumentException("meetingLink must not be blank");
        }
        this.platform = platform;
        this.meetingLink = meetingLink;
    }

    public String getPlatform() {
        return platform;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    /** Update only for a nonblank platform; otherwise leave unchanged. */
    public void setPlatform(String platform) {
        if (!isBlank(platform)) {
            this.platform = platform;
        }
    }

    /** Update only for a nonblank link; otherwise leave unchanged. */
    public void setMeetingLink(String meetingLink) {
        if (!isBlank(meetingLink)) {
            this.meetingLink = meetingLink;
        }
    }

    @Override
    public String getType() {
        return "ONLINE";
    }

    @Override
    public String toCSV() {
        return onlineFields(", ");
    }

    @Override
    public String toTSV() {
        return onlineFields("\t");
    }

    /** Meeting's fields (common data + invitees + agenda) plus platform and link. */
    private String onlineFields(String delimiter) {
        return meetingFields(delimiter) + delimiter + getPlatform()
                + delimiter + getMeetingLink();
    }
}
