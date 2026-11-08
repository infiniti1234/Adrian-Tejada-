package edu.fiu.cop3337.calendar.app;

import edu.fiu.cop3337.calendar.model.Appointment;
import edu.fiu.cop3337.calendar.model.CalendarDate;
import edu.fiu.cop3337.calendar.model.CalendarEvent;
import edu.fiu.cop3337.calendar.model.Meeting;
import edu.fiu.cop3337.calendar.model.OnlineMeeting;
import edu.fiu.cop3337.calendar.model.Task;
import edu.fiu.cop3337.calendar.view.MonthView;

import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 * CalendarApp is the main class for Homework Assignment 2 (Calendar Organizer
 * Phase 2). It keeps the HW1 program flow, but stores every kind of calendar
 * item in one ArrayList<CalendarEvent> and relies on dynamic binding for
 * printing.
 */
public class CalendarApp {
    public static final String UNKNOWN_COMMAND =
            "Unknown command. Type 'help' to see valid commands.";
    public static final String NO_EVENTS = "No events found.";

    public static void main(String[] args) {
        // Swing is used only for these two simple dialogs.
        JOptionPane.showMessageDialog(null,
                "Welcome to Calendar Organizer - Phase 2!",
                "COP 3337 Calendar Organizer",
                JOptionPane.INFORMATION_MESSAGE);

        Scanner keyboard = new Scanner(System.in);

        // One list holds CalendarEvent, Meeting, OnlineMeeting, Appointment,
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
            CalendarEvent event = parseItem(line);

            if (event == null) {
                System.out.println("Invalid input. Please re-enter the item.");
            } else {
                events.add(event);
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
                "COP 3337 Calendar Organizer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Parses one input line. The first token decides which class to build.
     *
     * The first 7 fields are the same for every type, so they are validated
     * once; then the subclass-specific fields are validated. An object is only
     * constructed after every check passes. Invalid input returns null.
     */
    public static CalendarEvent parseItem(String line) {
        if (line == null) {
            return null;
        }

        // The -1 limit keeps empty trailing fields so a blank last value is
        // still counted (and then rejected by the blank checks).
        String[] fields = line.split(",", -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        String type = fields[0];
        int expectedFields;
        if (type.equals("EVENT")) {
            expectedFields = 7;
        } else if (type.equals("MEETING")) {
            expectedFields = 9;
        } else if (type.equals("ONLINE")) {
            expectedFields = 11;
        } else if (type.equals("APPOINTMENT")) {
            expectedFields = 8;
        } else if (type.equals("TASK")) {
            expectedFields = 9;
        } else {
            return null;   // unknown item type
        }

        if (fields.length != expectedFields) {
            return null;
        }

        // ---- Common fields (HW1 rules) ----
        String title = fields[1];
        String startTime = fields[3];
        String owner = fields[5];
        String location = fields[6];

        if (isBlank(title) || isBlank(owner) || isBlank(location)) {
            return null;
        }

        CalendarDate date = parseDate(fields[2]);
        if (date == null) {
            return null;
        }

        if (!CalendarEvent.isValidStartTime(startTime)) {
            return null;
        }

        int duration = parseNonNegativeInt(fields[4]);
        if (duration <= 0) {
            return null;
        }

        // ---- Type-specific fields ----
        if (type.equals("EVENT")) {
            return new CalendarEvent(title, date, startTime, duration, owner, location);
        }

        if (type.equals("MEETING") || type.equals("ONLINE")) {
            int invitees = parseNonNegativeInt(fields[7]);   // -1 means invalid
            String agenda = fields[8];
            if (invitees < 0 || isBlank(agenda)) {
                return null;
            }

            if (type.equals("MEETING")) {
                return new Meeting(title, date, startTime, duration, owner, location,
                                   invitees, agenda);
            }

            String platform = fields[9];
            String meetingLink = fields[10];
            if (isBlank(platform) || isBlank(meetingLink)) {
                return null;
            }
            return new OnlineMeeting(title, date, startTime, duration, owner, location,
                                     invitees, agenda, platform, meetingLink);
        }

        if (type.equals("APPOINTMENT")) {
            String withWhom = fields[7];
            if (isBlank(withWhom)) {
                return null;
            }
            return new Appointment(title, date, startTime, duration, owner, location,
                                   withWhom);
        }

        // Only TASK is left at this point.
        int priority = parseNonNegativeInt(fields[7]);
        if (priority < 1 || priority > 5) {
            return null;
        }

        String completedText = fields[8];
        boolean completed;
        if (completedText.equalsIgnoreCase("true")) {
            completed = true;
        } else if (completedText.equalsIgnoreCase("false")) {
            completed = false;
        } else {
            return null;
        }

        return new Task(title, date, startTime, duration, owner, location,
                        priority, completed);
    }

    /**
     * Parses MM/DD/YYYY. Returns null if there are not exactly 3
     * slash-separated integer parts or the date is not valid.
     * Only constructs CalendarDate after validation passes.
     */
    public static CalendarDate parseDate(String dateText) {
        if (dateText == null) {
            return null;
        }

        String[] parts = dateText.trim().split("/", -1);
        if (parts.length != 3) {
            return null;
        }

        int month = parseNonNegativeInt(parts[0]);
        int day = parseNonNegativeInt(parts[1]);
        int year = parseNonNegativeInt(parts[2]);
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
     * Valid month: 1-12. Valid year: > 0. Returns null for invalid input.
     */
    public static int[] parseMonthYear(String monthText) {
        if (monthText == null) {
            return null;
        }

        String[] parts = monthText.trim().split("/", -1);
        if (parts.length != 2) {
            return null;
        }

        int month = parseNonNegativeInt(parts[0]);
        int year = parseNonNegativeInt(parts[1]);
        if (month < 1 || month > 12 || year <= 0) {
            return null;
        }

        int[] result = {month, year};
        return result;
    }

    /**
     * Helper: converts a string made only of digits (1-9 characters) into an int.
     * Returns -1 if the text is blank, contains a non-digit (including a minus
     * sign), or is too long. "0" returns 0, so callers check the range they need.
     */
    public static int parseNonNegativeInt(String text) {
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
            if (!Character.isDigit(c)) {
                return -1;
            }
            value = value * 10 + (c - '0');
        }
        return value;
    }

    /**
     * Processes one command. A malformed command or argument prints the
     * unknown-command message.
     */
    public static void processCommand(String command, ArrayList<CalendarEvent> events) {
        if (command == null) {
            System.out.println(UNKNOWN_COMMAND);
            return;
        }

        String text = command.trim();

        if (text.equals("print")) {
            printEvents(events);
        } else if (text.equals("help")) {
            printHelp();
        } else if (text.equals("types")) {
            printTypes(events);
        } else if (text.equals("online")) {
            printOnlineMeetings(events);
        } else if (text.startsWith("month ")) {
            int[] monthYear = parseMonthYear(text.substring(6));
            if (monthYear == null) {
                System.out.println(UNKNOWN_COMMAND);
            } else {
                MonthView view = new MonthView(monthYear[0], monthYear[1]);
                view.printMonth();
            }
        } else if (text.startsWith("on ")) {
            CalendarDate date = parseDate(text.substring(3));
            if (date == null) {
                System.out.println(UNKNOWN_COMMAND);
            } else {
                printEventsOnDate(events, date);
            }
        } else if (text.startsWith("owned by ")) {
            String owner = text.substring(9).trim();
            if (isBlank(owner)) {
                System.out.println(UNKNOWN_COMMAND);
            } else {
                printEventsOwnedBy(events, owner);
            }
        } else if (text.startsWith("type is ")) {
            String requestedType = text.substring(8).trim();
            if (!isValidTypeName(requestedType)) {
                System.out.println(UNKNOWN_COMMAND);
            } else {
                printEventsOfType(events, requestedType);
            }
        } else {
            System.out.println(UNKNOWN_COMMAND);
        }
    }

    /** True when name is one of the five item types (case-insensitive). */
    public static boolean isValidTypeName(String name) {
        return name.equalsIgnoreCase("EVENT") || name.equalsIgnoreCase("MEETING")
                || name.equalsIgnoreCase("ONLINE") || name.equalsIgnoreCase("APPOINTMENT")
                || name.equalsIgnoreCase("TASK");
    }

    /**
     * Prints every item. println(event) calls toString(), and dynamic binding
     * picks the right subclass version, so no if/else chain is needed.
     */
    public static void printEvents(ArrayList<CalendarEvent> events) {
        if (events.isEmpty()) {
            System.out.println(NO_EVENTS);
            return;
        }
        for (CalendarEvent event : events) {
            System.out.println(event);
        }
    }

    /** Prints items on the given date, or "No events found." */
    public static void printEventsOnDate(ArrayList<CalendarEvent> events, CalendarDate date) {
        boolean found = false;
        for (CalendarEvent event : events) {
            if (event.occursOn(date)) {      // inherited by every subclass
                System.out.println(event);
                found = true;
            }
        }
        if (!found) {
            System.out.println(NO_EVENTS);
        }
    }

    /** Prints items owned by owner (case-insensitive), or "No events found." */
    public static void printEventsOwnedBy(ArrayList<CalendarEvent> events, String owner) {
        boolean found = false;
        for (CalendarEvent event : events) {
            if (event.isOwnedBy(owner)) {    // inherited by every subclass
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
        for (CalendarEvent event : events) {
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
     * next to the dynamic type (the class of the actual object).
     */
    public static void printTypes(ArrayList<CalendarEvent> events) {
        if (events.isEmpty()) {
            System.out.println(NO_EVENTS);
            return;
        }
        for (CalendarEvent event : events) {
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
        for (CalendarEvent event : events) {
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
        System.out.println("help");
        System.out.println("quit");
    }

    /** True for null or empty-after-trimming strings. */
    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }
}
