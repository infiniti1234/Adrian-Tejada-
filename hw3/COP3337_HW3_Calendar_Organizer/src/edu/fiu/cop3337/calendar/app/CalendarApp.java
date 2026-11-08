package edu.fiu.cop3337.calendar.app;

import edu.fiu.cop3337.calendar.model.Appointment;
import edu.fiu.cop3337.calendar.model.BasicEvent;
import edu.fiu.cop3337.calendar.model.CalendarDate;
import edu.fiu.cop3337.calendar.model.CalendarEvent;
import edu.fiu.cop3337.calendar.model.InvalidCalendarItemException;
import edu.fiu.cop3337.calendar.model.Meeting;
import edu.fiu.cop3337.calendar.model.OnlineMeeting;
import edu.fiu.cop3337.calendar.model.Task;
import edu.fiu.cop3337.calendar.view.MonthView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 * CalendarApp is the main class for Homework Assignment 3 (Calendar Organizer
 * Phase 3). It keeps the HW2 console program, stores every kind of calendar
 * item in one ArrayList<CalendarEvent>, and adds tab-separated import/export
 * plus Swing dialogs for errors, summaries, and overwrite confirmation.
 */
public class CalendarApp {
    public static final String APP_TITLE = "COP 3337 Calendar Organizer";
    public static final String UNKNOWN_COMMAND =
            "Unknown command. Type 'help' to see valid commands.";
    public static final String NO_EVENTS = "No events found.";

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null,
                "Welcome to Calendar Organizer - Phase 3!",
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE);

        Scanner keyboard = new Scanner(System.in);

        // One list holds BasicEvent, Meeting, OnlineMeeting, Appointment,
        // and Task objects, because each of them IS-A CalendarEvent.
        ArrayList<CalendarEvent> events = new ArrayList<>();

        System.out.println("Enter calendar items.");
        System.out.println("Formats:");
        System.out.println("EVENT, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location");
        System.out.println("MEETING, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location, invitees, agenda");
        System.out.println("ONLINE, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location, invitees, agenda, platform, meetingLink");
        System.out.println("APPOINTMENT, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location, withWhom");
        System.out.println("TASK, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location, priority, completed");
        System.out.println("Type Done when finished.");

        // -------------------------
        // Input Phase
        // -------------------------
        String line = keyboard.nextLine();

        while (!line.trim().equalsIgnoreCase("Done")) {
            try {
                events.add(parseItem(line, ","));
            } catch (InvalidCalendarItemException e) {
                showError(e.getMessage() + " Please re-enter the item.");
            }

            line = keyboard.nextLine();
        }

        // -------------------------
        // Command Phase
        // -------------------------
        System.out.println("Enter a command. Type help to see valid commands.");

        String command = keyboard.nextLine();

        while (!command.trim().equalsIgnoreCase("quit")) {
            processCommand(command, events);
            command = keyboard.nextLine();
        }

        keyboard.close();

        JOptionPane.showMessageDialog(null,
                "Goodbye from Calendar Organizer!",
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    // =====================================================================
    // Parsing
    // =====================================================================

    /**
     * Shared parser for keyboard records (delimiter ",") and file records
     * (delimiter "\t").
     *
     * The first token decides which class to build. The first 7 fields are the
     * same for every type, so they are validated once; then the type-specific
     * fields are validated. An object is only constructed after every check
     * passes. Any problem throws InvalidCalendarItemException with a message
     * that says which field is wrong.
     */
    public static CalendarEvent parseItem(String line, String delimiter)
            throws InvalidCalendarItemException {
        if (isBlank(line)) {
            throw new InvalidCalendarItemException("the record is blank.");
        }

        // The -1 limit keeps empty trailing fields so a missing last value is
        // still counted (and then rejected by the blank checks).
        String[] parts = line.split(delimiter, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        String type = parts[0];
        int expectedFields = expectedFieldCount(type);
        if (expectedFields < 0) {
            throw new InvalidCalendarItemException("unknown item type \"" + type
                    + "\". Use EVENT, MEETING, ONLINE, APPOINTMENT, or TASK.");
        }
        if (parts.length != expectedFields) {
            throw new InvalidCalendarItemException(type + " records need "
                    + expectedFields + " fields, but this one has " + parts.length + ".");
        }

        for (int i = 0; i < parts.length; i++) {
            if (hasCommaTabOrLineBreak(parts[i])) {
                throw new InvalidCalendarItemException("field " + (i + 1)
                        + " contains a comma, tab, or line break.");
            }
        }

        // ---- Common fields ----
        String title = requireText(parts[1], "title");
        CalendarDate date = parseDate(parts[2]);

        String startTime = parts[3];
        if (!CalendarEvent.isValidStartTime(startTime)) {
            throw new InvalidCalendarItemException("start time \"" + startTime
                    + "\" must use hh:mm am/pm, for example 09:30 am.");
        }

        int duration = parseIntField(parts[4], "durationMinutes");
        if (duration <= 0) {
            throw new InvalidCalendarItemException(
                    "durationMinutes must be greater than 0, but was " + duration + ".");
        }

        String owner = requireText(parts[5], "owner");
        String location = requireText(parts[6], "location");

        // ---- Type-specific fields ----
        int invitees = 0;
        String agenda = "";
        String platform = "";
        String meetingLink = "";
        String withWhom = "";
        int priority = 0;
        boolean completed = false;

        if (type.equals("MEETING") || type.equals("ONLINE")) {
            invitees = parseIntField(parts[7], "invitees");
            if (invitees < 0) {
                throw new InvalidCalendarItemException(
                        "invitees must be 0 or more, but was " + invitees + ".");
            }
            agenda = requireText(parts[8], "agenda");
        }
        if (type.equals("ONLINE")) {
            platform = requireText(parts[9], "platform");
            meetingLink = requireText(parts[10], "meetingLink");
        }
        if (type.equals("APPOINTMENT")) {
            withWhom = requireText(parts[7], "withWhom");
        }
        if (type.equals("TASK")) {
            priority = parseIntField(parts[7], "priority");
            if (priority < 1 || priority > 5) {
                throw new InvalidCalendarItemException(
                        "priority must be 1 through 5, but was " + priority + ".");
            }
            completed = parseBooleanField(parts[8], "completed");
        }

        // The constructors also protect their objects. If one of them still
        // rejects a value, wrap its exception at this parser boundary.
        try {
            if (type.equals("EVENT")) {
                return new BasicEvent(title, date, startTime, duration, owner, location);
            } else if (type.equals("MEETING")) {
                return new Meeting(title, date, startTime, duration, owner, location,
                                   invitees, agenda);
            } else if (type.equals("ONLINE")) {
                return new OnlineMeeting(title, date, startTime, duration, owner, location,
                                         invitees, agenda, platform, meetingLink);
            } else if (type.equals("APPOINTMENT")) {
                return new Appointment(title, date, startTime, duration, owner, location,
                                       withWhom);
            } else {
                return new Task(title, date, startTime, duration, owner, location,
                                priority, completed);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidCalendarItemException("could not create the " + type
                    + " item: " + e.getMessage() + ".", e);
        }
    }

    /** Number of fields a record of this type must have, or -1 for an unknown type. */
    public static int expectedFieldCount(String type) {
        if (type.equals("EVENT")) {
            return 7;
        } else if (type.equals("MEETING")) {
            return 9;
        } else if (type.equals("ONLINE")) {
            return 11;
        } else if (type.equals("APPOINTMENT")) {
            return 8;
        } else if (type.equals("TASK")) {
            return 9;
        } else {
            return -1;
        }
    }

    /** True if text contains a comma, tab, or line break. */
    public static boolean hasCommaTabOrLineBreak(String text) {
        return text.indexOf(',') >= 0 || text.indexOf('\t') >= 0
                || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0;
    }

    /** Returns text if it is nonblank; otherwise throws with the field name. */
    public static String requireText(String text, String fieldName)
            throws InvalidCalendarItemException {
        if (isBlank(text)) {
            throw new InvalidCalendarItemException(fieldName + " must not be blank.");
        }
        return text;
    }

    /**
     * Parses a whole number that must fit in an int. Only parsing happens here;
     * callers check the allowed range separately. A NumberFormatException is
     * wrapped, with the field name in the message and the original as the cause.
     */
    public static int parseIntField(String text, String fieldName)
            throws InvalidCalendarItemException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidCalendarItemException(fieldName
                    + " must be a whole number that fits in an int, but was \""
                    + text + "\".", e);
        }
    }

    /** Accepts only "true" or "false" (ignoring case and surrounding spaces). */
    public static boolean parseBooleanField(String text, String fieldName)
            throws InvalidCalendarItemException {
        String value = text.trim();
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new InvalidCalendarItemException(fieldName
                    + " must be true or false, but was \"" + text + "\".");
        }
    }

    /**
     * Parses MM/DD/YYYY. Throws if there are not exactly 3 slash-separated
     * integer parts or the date does not exist (including leap-year checks).
     * Only constructs CalendarDate after validation passes.
     */
    public static CalendarDate parseDate(String dateText)
            throws InvalidCalendarItemException {
        if (isBlank(dateText)) {
            throw new InvalidCalendarItemException("date must not be blank; use MM/DD/YYYY.");
        }

        String[] parts = dateText.trim().split("/", -1);
        if (parts.length != 3) {
            throw new InvalidCalendarItemException("date \"" + dateText.trim()
                    + "\" must use MM/DD/YYYY.");
        }

        int month = parseIntField(parts[0], "date month");
        int day = parseIntField(parts[1], "date day");
        int year = parseIntField(parts[2], "date year");

        if (!CalendarDate.isValidDate(month, day, year)) {
            throw new InvalidCalendarItemException("date \"" + dateText.trim()
                    + "\" is not a real date (check the month, day, year, and leap year).");
        }

        return new CalendarDate(month, day, year);
    }

    /**
     * Parses MM/YYYY and returns {month, year}.
     * Valid month: 1-12. Valid year: > 0.
     * Throws IllegalArgumentException with a helpful message for invalid input.
     */
    public static int[] parseMonthYear(String monthText) {
        String[] parts = monthText.trim().split("/", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("month \"" + monthText.trim()
                    + "\" must use MM/YYYY, for example 03/2026.");
        }

        int month;
        int year;
        try {
            month = Integer.parseInt(parts[0].trim());
            year = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("month \"" + monthText.trim()
                    + "\" must contain whole numbers in MM/YYYY.", e);
        }

        if (month < 1 || month > 12 || year <= 0) {
            throw new IllegalArgumentException("month \"" + monthText.trim()
                    + "\" needs a month from 01 to 12 and a year greater than 0.");
        }

        int[] result = {month, year};
        return result;
    }

    // =====================================================================
    // Commands
    // =====================================================================

    /**
     * Processes one command. A malformed command or missing argument prints a
     * helpful error and shows an error dialog, then returns to the command loop.
     */
    public static void processCommand(String command, ArrayList<CalendarEvent> events) {
        String text = "";
        if (command != null) {
            text = command.trim();
        }

        if (text.equals("print")) {
            printEvents(events);
        } else if (text.equals("help")) {
            printHelp();
        } else if (text.equals("types")) {
            printTypes(events);
        } else if (text.equals("online")) {
            printOnlineMeetings(events);
        } else if (isCommand(text, "month")) {
            monthCommand(argumentOf(text, "month"));
        } else if (isCommand(text, "on")) {
            onCommand(argumentOf(text, "on"), events);
        } else if (isCommand(text, "owned by")) {
            String owner = argumentOf(text, "owned by");
            if (isBlank(owner)) {
                showError("Missing owner name. Usage: owned by ownerName");
            } else {
                printEventsOwnedBy(events, owner);
            }
        } else if (isCommand(text, "type is")) {
            String requestedType = argumentOf(text, "type is");
            if (isBlank(requestedType)) {
                showError("Missing type. Usage: type is TYPE");
            } else if (!isValidTypeName(requestedType)) {
                showError("Unknown type \"" + requestedType
                        + "\". Use EVENT, MEETING, ONLINE, APPOINTMENT, or TASK.");
            } else {
                printEventsOfType(events, requestedType);
            }
        } else if (isCommand(text, "import")) {
            importCommand(argumentOf(text, "import"), events);
        } else if (isCommand(text, "export")) {
            exportCommand(argumentOf(text, "export"), events);
        } else {
            showError(UNKNOWN_COMMAND);
        }
    }

    /** True when text is exactly the keyword or the keyword followed by a space. */
    public static boolean isCommand(String text, String keyword) {
        return text.equals(keyword) || text.startsWith(keyword + " ");
    }

    /** The trimmed text after the keyword (empty if there is none). */
    public static String argumentOf(String text, String keyword) {
        return text.substring(keyword.length()).trim();
    }

    /** month MM/YYYY: builds and prints the 6 x 7 month view. */
    public static void monthCommand(String argument) {
        if (isBlank(argument)) {
            showError("Missing month. Usage: month MM/YYYY");
            return;
        }
        try {
            int[] monthYear = parseMonthYear(argument);
            MonthView view = new MonthView(monthYear[0], monthYear[1]);
            view.printMonth();
        } catch (IllegalArgumentException e) {
            showError("Invalid month: " + e.getMessage());
        }
    }

    /** on MM/DD/YYYY: prints the items on that date. */
    public static void onCommand(String argument, ArrayList<CalendarEvent> events) {
        if (isBlank(argument)) {
            showError("Missing date. Usage: on MM/DD/YYYY");
            return;
        }
        try {
            CalendarDate date = parseDate(argument);
            printEventsOnDate(events, date);
        } catch (InvalidCalendarItemException e) {
            showError(e.getMessage());
        }
    }

    /** True when name is one of the five item types (case-insensitive). */
    public static boolean isValidTypeName(String name) {
        return name.equalsIgnoreCase("EVENT") || name.equalsIgnoreCase("MEETING")
                || name.equalsIgnoreCase("ONLINE") || name.equalsIgnoreCase("APPOINTMENT")
                || name.equalsIgnoreCase("TASK");
    }

    /**
     * Returns a sorted copy of events, so the stored list keeps its entry order
     * and every record (including duplicates) is kept.
     */
    public static ArrayList<CalendarEvent> sortedCopy(ArrayList<CalendarEvent> events) {
        ArrayList<CalendarEvent> sorted = new ArrayList<>(events);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Prints every item in sorted order. println(event) calls toString(), and
     * dynamic binding picks the right subclass toCSV(), so no if/else chain is
     * needed.
     */
    public static void printEvents(ArrayList<CalendarEvent> events) {
        if (events.isEmpty()) {
            System.out.println(NO_EVENTS);
            return;
        }
        for (CalendarEvent event : sortedCopy(events)) {
            System.out.println(event);
        }
    }

    /** Prints items on the given date in sorted order, or "No events found." */
    public static void printEventsOnDate(ArrayList<CalendarEvent> events, CalendarDate date) {
        boolean found = false;
        for (CalendarEvent event : sortedCopy(events)) {
            if (event.occursOn(date)) {
                System.out.println(event);
                found = true;
            }
        }
        if (!found) {
            System.out.println(NO_EVENTS);
        }
    }

    /** Prints items owned by owner (case-insensitive) in sorted order, or "No events found." */
    public static void printEventsOwnedBy(ArrayList<CalendarEvent> events, String owner) {
        boolean found = false;
        for (CalendarEvent event : sortedCopy(events)) {
            if (event.isOwnedBy(owner)) {
                System.out.println(event);
                found = true;
            }
        }
        if (!found) {
            System.out.println(NO_EVENTS);
        }
    }

    /**
     * type is TYPE: getType() is called through a CalendarEvent reference,
     * and the overridden version for the actual object runs (dynamic binding).
     */
    public static void printEventsOfType(ArrayList<CalendarEvent> events, String requestedType) {
        boolean found = false;
        for (CalendarEvent event : sortedCopy(events)) {
            if (event.getType().equalsIgnoreCase(requestedType)) {
                System.out.println(event);
                found = true;
            }
        }
        if (!found) {
            System.out.println(NO_EVENTS);
        }
    }

    /**
     * types: shows the static type (the loop variable is always CalendarEvent)
     * next to the dynamic type (the class of the actual object). An EVENT
     * record now reports dynamic=BasicEvent.
     */
    public static void printTypes(ArrayList<CalendarEvent> events) {
        if (events.isEmpty()) {
            System.out.println(NO_EVENTS);
            return;
        }
        for (CalendarEvent event : sortedCopy(events)) {
            System.out.println("static=CalendarEvent, dynamic="
                    + event.getClass().getSimpleName()
                    + ", type=" + event.getType());
        }
    }

    /**
     * online: getPlatform() and getMeetingLink() exist only in OnlineMeeting,
     * so we check with instanceof first and then safely downcast.
     */
    public static void printOnlineMeetings(ArrayList<CalendarEvent> events) {
        boolean found = false;
        for (CalendarEvent event : sortedCopy(events)) {
            if (event instanceof OnlineMeeting) {
                OnlineMeeting online = (OnlineMeeting) event;
                System.out.println(online.getTitle() + ", " + online.getPlatform()
                        + ", " + online.getMeetingLink());
                found = true;
            }
        }
        if (!found) {
            System.out.println(NO_EVENTS);
        }
    }

    /** Prints the valid command list. */
    public static void printHelp() {
        System.out.println("Valid commands:");
        System.out.println("print");
        System.out.println("month MM/YYYY");
        System.out.println("on MM/DD/YYYY");
        System.out.println("owned by ownerName");
        System.out.println("type is TYPE");
        System.out.println("types");
        System.out.println("online");
        System.out.println("import filename");
        System.out.println("export filename");
        System.out.println("help");
        System.out.println("quit");
    }

    // =====================================================================
    // File import / export
    // =====================================================================

    /**
     * Returns the trimmed file name, or throws IllegalArgumentException when it
     * is missing. Called before any stream is opened.
     */
    public static String requireFileName(String fileName, String commandName) {
        if (isBlank(fileName)) {
            throw new IllegalArgumentException("Missing file name. Usage: "
                    + commandName + " filename");
        }
        return fileName.trim();
    }

    /** import filename: reads tab-separated records and reports the counts. */
    public static void importCommand(String fileName, ArrayList<CalendarEvent> events) {
        try {
            File file = new File(requireFileName(fileName, "import"));
            int[] counts = importEvents(file, events);
            showSummary("Import complete from " + file.getPath() + ": "
                    + counts[0] + " imported, " + counts[1] + " skipped.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Import failed: " + e.getMessage());
        } catch (SecurityException e) {
            showError("Import failed: permission denied (" + e.getMessage() + ").");
        }
    }

    /**
     * Reads file one line at a time with Scanner (UTF-8) and adds every valid
     * record to events. An invalid row is reported with its line number and
     * skipped, and reading continues. Returns {imported, skipped}.
     *
     * If reading stops because of an I/O error, the records already added stay
     * in the list and an IOException with the partial counts is thrown.
     */
    public static int[] importEvents(File file, ArrayList<CalendarEvent> events)
            throws IOException {
        int imported = 0;
        int skipped = 0;
        int lineNumber = 0;

        try (Scanner in = new Scanner(file, StandardCharsets.UTF_8)) {
            while (in.hasNextLine()) {
                String line = in.nextLine();
                lineNumber++;
                try {
                    events.add(parseItem(line, "\t"));
                    imported++;
                } catch (InvalidCalendarItemException e) {
                    skipped++;
                    System.out.println("Skipped line " + lineNumber + ": " + e.getMessage());
                }
            }

            // Scanner hides read errors; ioException() reports the last one.
            IOException readError = in.ioException();
            if (readError != null) {
                throw new IOException("reading " + file.getPath() + " stopped after line "
                        + lineNumber + " (" + imported + " imported, " + skipped
                        + " skipped before the error; the imported items were kept): "
                        + readError, readError);
            }
        }

        int[] counts = {imported, skipped};
        return counts;
    }

    /**
     * export filename: asks before replacing an existing file, writes the
     * sorted records, and reports how many were written.
     */
    public static void exportCommand(String fileName, ArrayList<CalendarEvent> events) {
        try {
            File target = new File(requireFileName(fileName, "export"));
            if (target.isDirectory()) {
                throw new IOException(target.getPath() + " is a folder, not a file.");
            }
            if (target.exists() && !confirmOverwrite(target)) {
                System.out.println("Export canceled.");
                return;
            }

            int written = exportEvents(target, events);
            showSummary("Export complete: " + written + " items written to "
                    + target.getPath() + ".");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        } catch (SecurityException e) {
            showError("Export failed: permission denied (" + e.getMessage() + ").");
        }
    }

    /**
     * Writes a sorted copy of events to target, one toTSV() record per line,
     * with no header. false in FileOutputStream means replace (not append);
     * false in PrintWriter turns autoFlush off. Returns the number written,
     * only after the writer has been closed with no errors.
     */
    public static int exportEvents(File target, ArrayList<CalendarEvent> events)
            throws IOException {
        ArrayList<CalendarEvent> sorted = sortedCopy(events);

        PrintWriter out = new PrintWriter(
                new FileOutputStream(target, false), false, StandardCharsets.UTF_8);
        try (out) {
            for (CalendarEvent event : sorted) {
                out.println(event.toTSV());
            }
        }

        // PrintWriter never throws IOException; checkError() reports any
        // write, flush, or close failure.
        if (out.checkError()) {
            throw new IOException("Write or close failed.");
        }
        return sorted.size();
    }

    // =====================================================================
    // Swing dialogs
    // =====================================================================

    /** Prints message and shows it in an error dialog. */
    public static void showError(String message) {
        System.out.println(message);
        JOptionPane.showMessageDialog(null, message, APP_TITLE,
                JOptionPane.ERROR_MESSAGE);
    }

    /** Prints message and shows it in an information dialog. */
    public static void showSummary(String message) {
        System.out.println(message);
        JOptionPane.showMessageDialog(null, message, APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Asks before replacing an existing file. Only YES authorizes the export;
     * NO or closing the dialog cancels it.
     */
    public static boolean confirmOverwrite(File target) {
        int choice = JOptionPane.showConfirmDialog(null,
                target.getPath() + " already exists. Replace it?",
                APP_TITLE,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    /** True for null or empty-after-trimming strings. */
    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }
}
