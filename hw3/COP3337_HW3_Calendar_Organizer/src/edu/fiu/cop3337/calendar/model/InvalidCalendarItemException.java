package edu.fiu.cop3337.calendar.model;

/**
 * Thrown when a calendar record (typed at the keyboard or read from a file)
 * cannot be turned into a valid calendar item.
 *
 * This is a checked exception: it extends Exception, not RuntimeException,
 * so every method that can throw it must catch it or declare it.
 */
public class InvalidCalendarItemException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidCalendarItemException(String message) {
        super(message);
    }

    /** Keeps the original exception (for example a NumberFormatException) as the cause. */
    public InvalidCalendarItemException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Adds an application-specific prefix so the message is clear wherever it is printed. */
    @Override
    public String getMessage() {
        return "Invalid calendar item: " + super.getMessage();
    }
}
