package edu.fiu.cop3337.calendar.model;

/**
 * A CalendarRecord can turn itself into one line of text.
 *
 * toCSV() is the comma-separated console format and toTSV() is the
 * tab-separated file format. Both list the same fields in the same order.
 */
public interface CalendarRecord {
    String toCSV();

    String toTSV();
}
