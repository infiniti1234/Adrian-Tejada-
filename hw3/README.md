# Calendar Organizer - Phase 3 (COP 3337 HW3)

```
hw3/
  src/edu/fiu/cop3337/calendar/
    app/CalendarApp.java              main, shared parser, commands, import/export
    model/CalendarDate.java
    model/CalendarRecord.java         interface: toCSV(), toTSV()
    model/CalendarEvent.java          abstract, implements CalendarRecord, Comparable<CalendarEvent>
    model/BasicEvent.java             EVENT
    model/Meeting.java                MEETING  (+ invitees, agenda)
    model/OnlineMeeting.java          ONLINE   (Meeting + platform, meetingLink)
    model/Appointment.java            APPOINTMENT (+ withWhom)
    model/Task.java                   TASK     (+ priority 1-5, completed)
    model/InvalidCalendarItemException.java   checked exception
    view/MonthView.java               int[6][7] month grid
    view/Dialogs.java                 small JOptionPane helpers
  data/input/                         test input files (tab-delimited)
  data/output/                        exported files
  tests/                              logs, dialog screenshots, DialogCapture helper
  TESTING.md                          test table
  GenAI_Attribution.txt
```

## Build and run (from `hw3/`)

```bash
javac -d out $(find src -name '*.java')
java -cp out edu.fiu.cop3337.calendar.app.CalendarApp
```

Enter items as comma-separated lines and type `Done` when finished. Then use the
commands: `print`, `month MM/YYYY`, `on MM/DD/YYYY`, `owned by <name>`,
`type is <TYPE>`, `types`, `online`, `import <file>`, `export <file>`, `help`, `quit`.

Example: `import data/input/mixed.txt` imports 5 items and skips 3 rows.

If no display is available (`java.awt.headless=true`), dialogs are printed as
`[dialog ...]` lines. The overwrite question is then answered by
`-Dcalendar.confirmOverwrite=yes|no` (default `no`), so a headless run never
overwrites a file by accident.

## Note on Phase 2

This repository had no HW#2 code, only HW1. The Phase 2 parts the assignment
depends on (packages, the four subclasses, the short and full constructors,
`occursOn` overloads, the `type is`/`types`/`online` commands, `ArrayList`) were
rebuilt from the HW1 code. If your own HW#2 differs (for example, the exact
short-constructor parameters or the `types` output format), merge those details in.
