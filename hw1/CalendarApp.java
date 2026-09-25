import java.util.Scanner;

/**
 * CalendarApp is the main class for Homework Assignment 1.
 *
 * GIVEN in this starter file:
 * - main program structure
 * - fixed CalendarEvent[100] storage
 * - event-entry loop
 * - command loop
 * - required method headers
 * - printHelp(...) and isBlank(...)
 *
 * STUDENT WORK:
 * Complete the TODO methods without replacing the required arrays or using
 * later-course features such as ArrayList or Java date/calendar libraries.
 */
public class CalendarApp {
    public static final int MAX_EVENTS = 100;

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);

        CalendarEvent[] events = new CalendarEvent[MAX_EVENTS];
        int eventCount = 0;

        System.out.println("Enter calendar events.");
        System.out.println("Format:");
        System.out.println("EVENT, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location");
        System.out.println("Type Done when finished.");

        // -------------------------
        // User Input Phase - GIVEN
        // -------------------------
        String line = keyboard.nextLine();

        while (!line.equalsIgnoreCase("Done")) {
            CalendarEvent event = parseEvent(line);

            if (event == null) {
                System.out.println("Invalid input. Please re-enter the event.");
            } else if (eventCount >= MAX_EVENTS) {
                System.out.println("Event array is full. Cannot add more events.");
            } else {
                events[eventCount] = event;
                eventCount++;
            }

            line = keyboard.nextLine();
        }

        // -------------------------
        // Command Phase - GIVEN
        // -------------------------
        System.out.println("Enter a command. Type help to see valid commands.");

        String command = keyboard.nextLine();

        while (!command.equalsIgnoreCase("quit")) {
            processCommand(command, events, eventCount);
            command = keyboard.nextLine();
        }

        keyboard.close();
    }

    /**
     * Parses one event line.
     *
     * Required format:
     * EVENT, title, MM/DD/YYYY, hh:mm am/pm, durationMinutes, owner, location
     *
     * IMPORTANT:
     * Validate ALL user input before constructing CalendarDate/CalendarEvent.
     * Invalid input returns null. Do not create a default date/event.
     */
    public static CalendarEvent parseEvent(String line) {
        if (line == null) {
            return null;
        }

        // The -1 limit keeps trailing empty fields, so "EVENT, a, ..., " still
        // counts as 7 fields and is then rejected as a blank location.
        String[] fields = line.split(",", -1);
        if (fields.length != 7) {
            return null;
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        if (!fields[0].equals("EVENT")) {
            return null;
        }

        String title = fields[1];
        String owner = fields[5];
        String location = fields[6];
        if (isBlank(title) || isBlank(owner) || isBlank(location)) {
            return null;
        }

        CalendarDate date = parseDate(fields[2]);
        if (date == null) {
            return null;
        }

        String startTime = fields[3];
        if (!CalendarEvent.isValidStartTime(startTime)) {
            return null;
        }

        int durationMinutes = parsePositiveInt(fields[4]);
        if (durationMinutes <= 0) {
            return null;
        }

        return new CalendarEvent(title, date, startTime, durationMinutes, owner, location);
    }

    /**
     * Parses MM/DD/YYYY.
     *
     * Return null if:
     * - format does not have exactly 3 slash-separated integer parts, or
     * - CalendarDate.isValidDate(month, day, year) is false.
     *
     * Only construct CalendarDate AFTER validation passes.
     */
    public static CalendarDate parseDate(String dateText) {
        if (dateText == null) {
            return null;
        }

        String[] parts = dateText.trim().split("/", -1);
        if (parts.length != 3) {
            return null;
        }

        int month = parsePositiveInt(parts[0]);
        int day = parsePositiveInt(parts[1]);
        int year = parsePositiveInt(parts[2]);
        if (month < 0 || day < 0 || year < 0) {
            return null;
        }

        if (!CalendarDate.isValidDate(month, day, year)) {
            return null;
        }
        return new CalendarDate(month, day, year);
    }

    /**
     * Parses MM/YYYY and returns {month, year}.
     * Valid month: 1-12. Valid year: > 0.
     * Return null for invalid input.
     */
    public static int[] parseMonthYear(String monthText) {
        if (monthText == null) {
            return null;
        }

        String[] parts = monthText.trim().split("/", -1);
        if (parts.length != 2) {
            return null;
        }

        int month = parsePositiveInt(parts[0]);
        int year = parsePositiveInt(parts[1]);
        if (month < 1 || month > 12 || year <= 0) {
            return null;
        }

        int[] result = {month, year};
        return result;
    }

    /**
     * Converts text made only of digits (0-9) into an int.
     * Returns -1 if the text is blank, contains any other character
     * (such as a sign, decimal point, or space), or is too long to fit.
     */
    public static int parsePositiveInt(String text) {
        if (isBlank(text)) {
            return -1;
        }
        text = text.trim();
        if (text.length() > 9) {
            return -1;
        }

        int value = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            value = value * 10 + (c - '0');
        }
        return value;
    }

    /**
     * Processes one command.
     *
     * Valid commands:
     * print
     * month MM/YYYY
     * on MM/DD/YYYY
     * owned by ownerName
     * help
     * quit
     *
     * A malformed command or malformed command argument should be handled as
     * an unknown command and print the required unknown-command message.
     */
    public static void processCommand(String command, CalendarEvent[] events, int eventCount) {
        String unknown = "Unknown command. Type 'help' to see valid commands.";

        if (command == null) {
            System.out.println(unknown);
            return;
        }

        String text = command.trim();
        String lower = text.toLowerCase();

        if (lower.equals("print")) {
            printEvents(events, eventCount);
        } else if (lower.startsWith("month ")) {
            int[] monthYear = parseMonthYear(text.substring(6));
            if (monthYear == null) {
                System.out.println(unknown);
            } else {
                MonthView view = new MonthView(monthYear[0], monthYear[1]);
                view.printMonth();
            }
        } else if (lower.startsWith("on ")) {
            CalendarDate date = parseDate(text.substring(3));
            if (date == null) {
                System.out.println(unknown);
            } else {
                printEventsOnDate(events, eventCount, date);
            }
        } else if (lower.startsWith("owned by ")) {
            String owner = text.substring(9).trim();
            if (isBlank(owner)) {
                System.out.println(unknown);
            } else {
                printEventsOwnedBy(events, eventCount, owner);
            }
        } else if (lower.equals("help")) {
            printHelp();
        } else {
            System.out.println(unknown);
        }
    }

    /** Prints all stored events, or "No events found." */
    public static void printEvents(CalendarEvent[] events, int eventCount) {
        if (eventCount == 0) {
            System.out.println("No events found.");
            return;
        }
        for (int i = 0; i < eventCount; i++) {
            System.out.println(events[i]);
        }
    }

    /** Prints events on date, or "No events found." */
    public static void printEventsOnDate(CalendarEvent[] events, int eventCount, CalendarDate date) {
        boolean found = false;
        for (int i = 0; i < eventCount; i++) {
            if (events[i].occursOn(date)) {
                System.out.println(events[i]);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No events found.");
        }
    }

    /** Prints events owned by owner, case-insensitively, or "No events found." */
    public static void printEventsOwnedBy(CalendarEvent[] events, int eventCount, String owner) {
        boolean found = false;
        for (int i = 0; i < eventCount; i++) {
            if (events[i].isOwnedBy(owner)) {
                System.out.println(events[i]);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No events found.");
        }
    }

    /** GIVEN: prints the valid command list. */
    public static void printHelp() {
        System.out.println("Valid commands:");
        System.out.println("print");
        System.out.println("month MM/YYYY");
        System.out.println("on MM/DD/YYYY");
        System.out.println("owned by ownerName");
        System.out.println("help");
        System.out.println("quit");
    }

    /** GIVEN: true for null or empty-after-trimming strings. */
    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }
}
