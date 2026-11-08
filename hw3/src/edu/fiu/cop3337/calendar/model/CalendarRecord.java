package edu.fiu.cop3337.calendar.model;

/**
 * A calendar item that can be written as one console row (CSV) or one
 * file row (TSV).
 */
public interface CalendarRecord {
    String toCSV();

    String toTSV();
}
