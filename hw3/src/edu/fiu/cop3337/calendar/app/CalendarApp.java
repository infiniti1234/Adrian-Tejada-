package edu.fiu.cop3337.calendar.app;

import edu.fiu.cop3337.calendar.model.Appointment;
import edu.fiu.cop3337.calendar.model.BasicEvent;
import edu.fiu.cop3337.calendar.model.CalendarDate;
import edu.fiu.cop3337.calendar.model.CalendarEvent;
import edu.fiu.cop3337.calendar.model.InvalidCalendarItemException;
import edu.fiu.cop3337.calendar.model.Meeting;
import edu.fiu.cop3337.calendar.model.OnlineMeeting;
import edu.fiu.cop3337.calendar.model.Task;
import edu.fiu.cop3337.calendar.view.Dialogs;
import edu.fiu.cop3337.calendar.view.MonthView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Calendar Organizer - Phase 3.
 *
 * Console item entry and commands, tab-delimited import/export, and
 * JOptionPane feedback for errors, summaries, and overwrite confirmation.
 */
public class CalendarApp {
    public static final String KEYBOARD_DELIMITER = ",";
    public static final String FILE_DELIMITER = "\t";

    private static final String UNKNOWN = "Unknown command. Type 'help' to see valid commands.";

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        ArrayList<CalendarEvent> events = new ArrayList<>();

        Dialogs.info("Welcome to Calendar Organizer - Phase 3!\n"
                + "Enter items in the console, then use commands such as import and export.");

        printEntryInstructions();

        // -------------------------
        // Item entry phase
        // -------------------------
        while (keyboard.hasNextLine()) {
            String line = keyboard.nextLine();
            if (line.trim().equalsIgnoreCase("Done")) {
                break;
            }
            try {
                CalendarEvent item = parseItem(line, KEYBOARD_DELIMITER);
                events.add(item);
                System.out.println("Added: " + item);
            } catch (InvalidCalendarItemException e) {
                Dialogs.error(e.getMessage() + "\nPlease re-enter the item.");
            }
        }

        // -------------------------
        // Command phase
        // -------------------------
        System.out.println("Enter a command. Type help to see valid commands.");
        while (keyboard.hasNextLine()) {
            String command = keyboard.nextLine();
            if (command.trim().equalsIgnoreCase("quit")) {
                break;
            }
            processCommand(command, events);
        }

        keyboard.close();
        Dialogs.info("Goodbye from Calendar Organizer - Phase 3!");
    }

    private static void printEntryInstructions() {
        System.out.println("Enter calendar items, one per line (comma-separated).");
        System.out.println("Formats:");
        System.out.println("EVENT, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location");
        System.out.println("MEETING, ...common fields..., invitees, agenda");
        System.out.println("ONLINE, ...common fields..., invitees, agenda, platform, meetingLink");
        System.out.println("APPOINTMENT, ...common fields..., withWhom");
        System.out.println("TASK, ...common fields..., priority(1-5), completed(true/false)");
        System.out.println("Type Done when finished.");
    }

    // =========================================================
    // Shared parser (keyboard uses ",", files use "\t")
    // =========================================================

    /**
     * Parses one keyboard or file record into the matching CalendarEvent
     * subclass.
     *
     * @param line      the raw record
     * @param delimiter "," for keyboard input or "\t" for file input
     * @return a valid calendar item (never null)
     * @throws InvalidCalendarItemException if the record is blank, has an
     *         unknown type, the wrong number of fields, or any invalid field
     */
    public static CalendarEvent parseItem(String line, String delimiter)
            throws InvalidCalendarItemException {
        if (isBlank(line)) {
            throw new InvalidCalendarItemException("the record is blank.");
        }

        String[] parts = line.split(delimiter, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        String type = parts[0];
        int expected = expectedFieldCount(type);
        if (parts.length != expected) {
            throw new InvalidCalendarItemException(type + " records need " + expected
                    + " fields but this one has " + parts.length + ".");
        }

        String title = requireField(parts[1], "title");
        CalendarDate date = parseDate(parts[2]);
        String startTime = parseStartTime(parts[3]);
        int duration = parseIntField(parts[4], "durationMinutes");
        if (duration <= 0) {
            throw new InvalidCalendarItemException(
                    "durationMinutes must be a positive integer, but was " + duration + ".");
        }
        String owner = requireField(parts[5], "owner");
        String location = requireField(parts[6], "location");

        try {
            switch (type) {
                case "EVENT":
                    return new BasicEvent(title, date, startTime, duration, owner, location);

                case "MEETING": {
                    int invitees = parseInvitees(parts[7]);
                    String agenda = requireField(parts[8], "agenda");
                    return new Meeting(title, date, startTime, duration, owner, location,
                            invitees, agenda);
                }

                case "ONLINE": {
                    int invitees = parseInvitees(parts[7]);
                    String agenda = requireField(parts[8], "agenda");
                    String platform = requireField(parts[9], "platform");
                    String link = requireField(parts[10], "meetingLink");
                    return new OnlineMeeting(title, date, startTime, duration, owner, location,
                            invitees, agenda, platform, link);
                }

                case "APPOINTMENT": {
                    String withWhom = requireField(parts[7], "withWhom");
                    return new Appointment(title, date, startTime, duration, owner, location,
                            withWhom);
                }

                case "TASK": {
                    int priority = parseIntField(parts[7], "priority");
                    if (!Task.isValidPriority(priority)) {
                        throw new InvalidCalendarItemException(
                                "priority must be from 1 through 5, but was " + priority + ".");
                    }
                    boolean completed = parseBooleanField(parts[8], "completed");
                    return new Task(title, date, startTime, duration, owner, location,
                            priority, completed);
                }

                default:
                    throw new InvalidCalendarItemException("unknown type '" + type + "'.");
            }
        } catch (IllegalArgumentException e) {
            // Constructors protect invariants; report their failures as calendar-data errors.
            throw new InvalidCalendarItemException(
                    "could not create " + type + ": " + e.getMessage() + ".", e);
        }
    }

    /** Total field count for a type token, or an exception for unknown types. */
    private static int expectedFieldCount(String type) throws InvalidCalendarItemException {
        switch (type) {
            case "EVENT":
                return 7;
            case "APPOINTMENT":
                return 8;
            case "MEETING":
            case "TASK":
                return 9;
            case "ONLINE":
                return 11;
            default:
                if (isBlank(type)) {
                    throw new InvalidCalendarItemException("the type field is blank.");
                }
                throw new InvalidCalendarItemException("unknown type '" + type
                        + "'. Use EVENT, MEETING, ONLINE, APPOINTMENT, or TASK (uppercase).");
        }
    }

    /** Requires nonblank text without embedded commas, tabs, or line breaks. */
    private static String requireField(String text, String fieldName)
            throws InvalidCalendarItemException {
        if (isBlank(text)) {
            throw new InvalidCalendarItemException(fieldName + " is required but was blank.");
        }
        if (CalendarEvent.containsSeparator(text)) {
            throw new InvalidCalendarItemException(fieldName
                    + " must not contain commas, tabs, or line breaks: '" + text + "'.");
        }
        return text.trim();
    }

    /**
     * Parses an int field. Parsing (NumberFormatException) is wrapped here;
     * range checks are done separately by the caller.
     */
    private static int parseIntField(String text, String fieldName)
            throws InvalidCalendarItemException {
        if (isBlank(text)) {
            throw new InvalidCalendarItemException(fieldName + " is required but was blank.");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidCalendarItemException(fieldName
                    + " must be a whole number that fits in an int, but was '" + text.trim() + "'.", e);
        }
    }

    private static int parseInvitees(String text) throws InvalidCalendarItemException {
        int invitees = parseIntField(text, "invitees");
        if (invitees < 0) {
            throw new InvalidCalendarItemException(
                    "invitees must be 0 or more, but was " + invitees + ".");
        }
        return invitees;
    }

    /** Accepts only true or false (any case) after trimming. */
    private static boolean parseBooleanField(String text, String fieldName)
            throws InvalidCalendarItemException {
        String value = text == null ? "" : text.trim();
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new InvalidCalendarItemException(fieldName
                + " must be true or false, but was '" + value + "'.");
    }

    private static String parseStartTime(String text) throws InvalidCalendarItemException {
        if (isBlank(text)) {
            throw new InvalidCalendarItemException("startTime is required but was blank.");
        }
        String time = text.trim();
        if (!CalendarEvent.isValidStartTime(time)) {
            throw new InvalidCalendarItemException("startTime '" + time
                    + "' must use hh:mm am/pm with hh 01-12 and mm 00-59 (for example 09:00 am).");
        }
        return time;
    }

    /**
     * Parses MM/DD/YYYY, including leap-year validation.
     *
     * @throws InvalidCalendarItemException for malformed or nonexistent dates
     */
    public static CalendarDate parseDate(String dateText) throws InvalidCalendarItemException {
        if (isBlank(dateText)) {
            throw new InvalidCalendarItemException("date is required but was blank.");
        }
        String text = dateText.trim();
        String[] parts = text.split("/", -1);
        if (parts.length != 3) {
            throw new InvalidCalendarItemException("date '" + text + "' must use MM/DD/YYYY.");
        }

        int month = parseIntField(parts[0], "date month");
        int day = parseIntField(parts[1], "date day");
        int year = parseIntField(parts[2], "date year");

        if (!CalendarDate.isValidDate(month, day, year)) {
            throw new InvalidCalendarItemException("date '" + text
                    + "' does not exist (check the month, the day, and leap years).");
        }
        return new CalendarDate(month, day, year);
    }

    /**
     * Parses MM/YYYY and returns {month, year}.
     *
     * @throws InvalidCalendarItemException for malformed input or out-of-range values
     */
    public static int[] parseMonthYear(String monthText) throws InvalidCalendarItemException {
        if (isBlank(monthText)) {
            throw new InvalidCalendarItemException("month is required but was blank.");
        }
        String text = monthText.trim();
        String[] parts = text.split("/", -1);
        if (parts.length != 2) {
            throw new InvalidCalendarItemException("month '" + text + "' must use MM/YYYY.");
        }
        int month = parseIntField(parts[0], "month");
        int year = parseIntField(parts[1], "year");
        if (month < 1 || month > 12 || year <= 0) {
            throw new InvalidCalendarItemException("month '" + text
                    + "' needs a month 1-12 and a positive year.");
        }
        return new int[] {month, year};
    }

    // =========================================================
    // Commands
    // =========================================================

    /**
     * Processes one command. Errors are reported in the console and in an
     * error dialog; the application keeps running.
     */
    public static void processCommand(String command, ArrayList<CalendarEvent> events) {
        String text = command == null ? "" : command.trim();
        String lower = text.toLowerCase();

        try {
            if (lower.equals("print")) {
                printSorted(events);
            } else if (isCommand(lower, "month")) {
                int[] monthYear = parseMonthYear(argument(text, "month", "MM/YYYY"));
                new MonthView(monthYear[0], monthYear[1]).printMonth();
            } else if (isCommand(lower, "on")) {
                CalendarDate date = parseDate(argument(text, "on", "MM/DD/YYYY"));
                printOnDate(events, date);
            } else if (isCommand(lower, "owned by")) {
                printOwnedBy(events, argument(text, "owned by", "an owner name"));
            } else if (isCommand(lower, "type is")) {
                printOfType(events, argument(text, "type is", "a TYPE such as MEETING"));
            } else if (lower.equals("types")) {
                printTypes(events);
            } else if (lower.equals("online")) {
                printOnline(events);
            } else if (isCommand(lower, "import")) {
                importCommand(argument(text, "import", "a filename"), events);
            } else if (isCommand(lower, "export")) {
                exportCommand(argument(text, "export", "a filename"), events);
            } else if (lower.equals("help")) {
                printHelp();
            } else {
                Dialogs.error(UNKNOWN);
            }
        } catch (InvalidCalendarItemException e) {
            Dialogs.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            Dialogs.error(e.getMessage());
        } catch (IOException e) {
            Dialogs.error(fileAction(lower) + " failed: " + e.getMessage());
        } catch (SecurityException e) {
            Dialogs.error(fileAction(lower) + " failed (not permitted): " + e.getMessage());
        }
    }

    private static String fileAction(String lowerCommand) {
        return isCommand(lowerCommand, "import") ? "Import" : "Export";
    }

    /** True if lower is exactly keyword or keyword followed by whitespace. */
    private static boolean isCommand(String lower, String keyword) {
        return lower.equals(keyword)
                || (lower.startsWith(keyword) && Character.isWhitespace(lower.charAt(keyword.length())));
    }

    /**
     * Returns the entire trimmed remainder after keyword.
     *
     * @throws IllegalArgumentException if the remainder is blank
     */
    private static String argument(String text, String keyword, String usage) {
        String rest = text.substring(keyword.length()).trim();
        if (rest.isEmpty()) {
            throw new IllegalArgumentException("The '" + keyword + "' command needs "
                    + usage + ", for example: " + example(keyword));
        }
        return rest;
    }

    private static String example(String keyword) {
        switch (keyword) {
            case "month":
                return "month 03/2026";
            case "on":
                return "on 03/01/2026";
            case "owned by":
                return "owned by Alex";
            case "type is":
                return "type is TASK";
            case "import":
                return "import data/input/events.txt";
            default:
                return "export data/output/events.txt";
        }
    }

    /** Returns a sorted copy; the stored list keeps every record, including duplicates. */
    private static ArrayList<CalendarEvent> sortedCopy(ArrayList<CalendarEvent> events) {
        ArrayList<CalendarEvent> copy = new ArrayList<>(events);
        Collections.sort(copy);
        return copy;
    }

    private static void printList(ArrayList<CalendarEvent> items) {
        if (items.isEmpty()) {
            System.out.println("No events found.");
            return;
        }
        Collections.sort(items);
        for (CalendarEvent item : items) {
            System.out.println(item); // dynamic dispatch: toString() -> subclass toCSV()
        }
    }

    public static void printSorted(ArrayList<CalendarEvent> events) {
        printList(sortedCopy(events));
    }

    public static void printOnDate(ArrayList<CalendarEvent> events, CalendarDate date) {
        ArrayList<CalendarEvent> matches = new ArrayList<>();
        for (CalendarEvent event : events) {
            if (event.occursOn(date)) {
                matches.add(event);
            }
        }
        printList(matches);
    }

    public static void printOwnedBy(ArrayList<CalendarEvent> events, String owner) {
        ArrayList<CalendarEvent> matches = new ArrayList<>();
        for (CalendarEvent event : events) {
            if (event.isOwnedBy(owner)) {
                matches.add(event);
            }
        }
        printList(matches);
    }

    public static void printOfType(ArrayList<CalendarEvent> events, String type) {
        ArrayList<CalendarEvent> matches = new ArrayList<>();
        for (CalendarEvent event : events) {
            if (event.getType().equalsIgnoreCase(type)) {
                matches.add(event);
            }
        }
        printList(matches);
    }

    /** Shows the static (declared) type and the dynamic (runtime) class of each item. */
    public static void printTypes(ArrayList<CalendarEvent> events) {
        ArrayList<CalendarEvent> sorted = sortedCopy(events);
        if (sorted.isEmpty()) {
            System.out.println("No events found.");
            return;
        }
        for (CalendarEvent event : sorted) {
            System.out.println(event.getTitle() + ": static=CalendarEvent, dynamic="
                    + event.getClass().getSimpleName() + ", type=" + event.getType());
        }
    }

    /** Lists online meetings; instanceof is checked before the downcast. */
    public static void printOnline(ArrayList<CalendarEvent> events) {
        boolean found = false;
        for (CalendarEvent event : sortedCopy(events)) {
            if (event instanceof OnlineMeeting) {
                OnlineMeeting online = (OnlineMeeting) event;
                System.out.println(online.getTitle() + " on " + online.getDate() + " at "
                        + online.getStartTime() + " | platform: " + online.getPlatform()
                        + " | link: " + online.getMeetingLink());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No events found.");
        }
    }

    public static void printHelp() {
        System.out.println("Valid commands:");
        System.out.println("print");
        System.out.println("month MM/YYYY");
        System.out.println("on MM/DD/YYYY");
        System.out.println("owned by ownerName");
        System.out.println("type is TYPE   (EVENT, MEETING, ONLINE, APPOINTMENT, TASK)");
        System.out.println("types");
        System.out.println("online");
        System.out.println("import filename   (tab-delimited, UTF-8)");
        System.out.println("export filename   (tab-delimited, UTF-8)");
        System.out.println("help");
        System.out.println("quit");
    }

    // =========================================================
    // Import / export
    // =========================================================

    private static void importCommand(String filename, ArrayList<CalendarEvent> events)
            throws IOException {
        int[] counts = importFile(filename, events);
        Dialogs.info("Import complete from " + filename + ":\n"
                + counts[0] + " imported, " + counts[1] + " skipped.");
    }

    /**
     * Reads tab-delimited records with Scanner and adds valid items to events.
     * Bad rows are reported and skipped. Items already added are kept even
     * if a read error happens later.
     *
     * @return {imported, skipped}
     * @throws IllegalArgumentException if filename is blank
     * @throws IOException if the file cannot be opened or a read error occurs
     */
    public static int[] importFile(String filename, ArrayList<CalendarEvent> events)
            throws IOException {
        if (isBlank(filename)) {
            throw new IllegalArgumentException("import needs a filename.");
        }
        File source = new File(filename.trim());
        int imported = 0;
        int skipped = 0;
        int lineNumber = 0;

        try (Scanner in = new Scanner(source, StandardCharsets.UTF_8)) {
            while (in.hasNextLine()) {
                String line = in.nextLine();
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("﻿")) {
                    line = line.substring(1); // ignore a UTF-8 byte-order mark
                }
                try {
                    events.add(parseItem(line, FILE_DELIMITER));
                    imported++;
                } catch (InvalidCalendarItemException e) {
                    skipped++;
                    System.out.println("  Line " + lineNumber + " skipped: " + e.getMessage());
                }
            }

            IOException readError = in.ioException();
            if (readError != null) {
                throw new IOException("read error in " + source.getPath()
                        + " after line " + lineNumber + " (" + imported + " imported, "
                        + skipped + " skipped; the " + imported
                        + " imported items were kept): " + readError.getMessage(), readError);
            }
        }
        return new int[] {imported, skipped};
    }

    private static void exportCommand(String filename, ArrayList<CalendarEvent> events)
            throws IOException {
        int written = exportFile(filename, events);
        if (written < 0) {
            System.out.println("Export canceled.");
        } else {
            Dialogs.info("Export complete: " + written + " item(s) written to " + filename + ".");
        }
    }

    /**
     * Writes all items, sorted, as tab-delimited lines with PrintWriter.
     * If the target exists, the user must confirm the overwrite first.
     *
     * @return number of items written, or -1 if the user canceled
     * @throws IllegalArgumentException if filename is blank
     * @throws IOException if the file cannot be opened, or writing/closing failed
     */
    public static int exportFile(String filename, ArrayList<CalendarEvent> events)
            throws IOException {
        if (isBlank(filename)) {
            throw new IllegalArgumentException("export needs a filename.");
        }
        File target = new File(filename.trim());

        if (target.isDirectory()) {
            throw new IOException(target.getPath() + " is a directory, not a file.");
        }
        if (target.exists() && !Dialogs.confirmOverwrite(target.getPath())) {
            return -1;
        }

        ArrayList<CalendarEvent> sorted = sortedCopy(events);

        PrintWriter out = new PrintWriter(
                new FileOutputStream(target, false), false, StandardCharsets.UTF_8);
        try (out) {
            for (CalendarEvent event : sorted) {
                out.println(event.toTSV()); // dynamic dispatch picks the subclass format
            }
        }
        if (out.checkError()) {
            throw new IOException("Write or close failed for " + target.getPath() + ".");
        }
        return sorted.size();
    }

    /** True for null or empty-after-trimming strings. */
    public static boolean isBlank(String text) {
        return CalendarEvent.isBlank(text);
    }
}
