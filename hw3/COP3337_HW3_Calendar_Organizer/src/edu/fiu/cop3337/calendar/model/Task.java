package edu.fiu.cop3337.calendar.model;

/**
 * A Task IS-A CalendarEvent with a priority (1-5) and a completed flag.
 */
public class Task extends CalendarEvent {
    private int priority;
    private boolean completed;

    /** Full constructor: builds the CalendarEvent part with super(...). */
    public Task(String title, CalendarDate date, String startTime,
                int durationMinutes, String owner, String location,
                int priority, boolean completed) {
        super(title, date, startTime, durationMinutes, owner, location);
        this.priority = priority;
        this.completed = completed;
    }

    /**
     * Shorter overloaded constructor without completed.
     * Uses this(...) to reuse the full constructor, defaulting completed to false.
     */
    public Task(String title, CalendarDate date, String startTime,
                int durationMinutes, String owner, String location,
                int priority) {
        this(title, date, startTime, durationMinutes, owner, location,
             priority, false);
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    /** Update only when priority is 1 through 5; otherwise leave unchanged. */
    public void setPriority(int priority) {
        if (priority >= 1 && priority <= 5) {
            this.priority = priority;
        }
    }

    /** Any boolean value is valid. */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String getType() {
        return "TASK";
    }

    /** Common event data plus priority and completed. */
    @Override
    public String toString() {
        return super.toString() + ", " + getPriority() + ", " + isCompleted();
    }
}
