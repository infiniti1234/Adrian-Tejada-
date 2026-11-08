package edu.fiu.cop3337.calendar.model;

/** TASK: common fields + priority (1-5), completed. */
public class Task extends CalendarEvent {
    public static final int MIN_PRIORITY = 1;
    public static final int MAX_PRIORITY = 5;

    private int priority;
    private boolean completed;

    public Task(String title, CalendarDate date, String startTime, int durationMinutes,
                String owner, String location, int priority, boolean completed) {
        super(title, date, startTime, durationMinutes, owner, location);
        if (!isValidPriority(priority)) {
            throw new IllegalArgumentException("priority must be 1 through 5");
        }
        this.priority = priority;
        this.completed = completed;
    }

    public Task(String title, CalendarDate date, String startTime, int durationMinutes,
                int priority, boolean completed) {
        this(title, date, startTime, durationMinutes, DEFAULT_OWNER, DEFAULT_LOCATION,
                priority, completed);
    }

    public static boolean isValidPriority(int priority) {
        return priority >= MIN_PRIORITY && priority <= MAX_PRIORITY;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setPriority(int priority) {
        if (isValidPriority(priority)) {
            this.priority = priority;
        }
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String getType() {
        return "TASK";
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
        return commonFields(delimiter) + delimiter + getPriority() + delimiter + isCompleted();
    }
}
