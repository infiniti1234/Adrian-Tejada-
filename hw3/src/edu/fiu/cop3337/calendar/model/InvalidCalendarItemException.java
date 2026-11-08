package edu.fiu.cop3337.calendar.model;

/**
 * Checked exception thrown when a keyboard or file record cannot become a
 * valid calendar item. Callers must catch or declare it.
 */
public class InvalidCalendarItemException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidCalendarItemException(String message) {
        super(message);
    }

    public InvalidCalendarItemException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return "Invalid calendar item: " + super.getMessage();
    }
}
