package edu.fiu.cop3337.calendar.model;

/** ONLINE: meeting fields + platform, meetingLink. */
public class OnlineMeeting extends Meeting {
    private String platform;
    private String meetingLink;

    public OnlineMeeting(String title, CalendarDate date, String startTime, int durationMinutes,
                         String owner, String location, int invitees, String agenda,
                         String platform, String meetingLink) {
        super(title, date, startTime, durationMinutes, owner, location, invitees, agenda);
        this.platform = requireText(platform, "platform");
        this.meetingLink = requireText(meetingLink, "meetingLink");
    }

    public OnlineMeeting(String title, CalendarDate date, String startTime, int durationMinutes,
                         int invitees, String agenda, String platform, String meetingLink) {
        this(title, date, startTime, durationMinutes, DEFAULT_OWNER, "Online",
                invitees, agenda, platform, meetingLink);
    }

    public String getPlatform() {
        return platform;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setPlatform(String platform) {
        if (isValidText(platform)) {
            this.platform = platform.trim();
        }
    }

    public void setMeetingLink(String meetingLink) {
        if (isValidText(meetingLink)) {
            this.meetingLink = meetingLink.trim();
        }
    }

    @Override
    public String getType() {
        return "ONLINE";
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
        return commonFields(delimiter) + delimiter + getInvitees() + delimiter + getAgenda()
                + delimiter + getPlatform() + delimiter + getMeetingLink();
    }
}
