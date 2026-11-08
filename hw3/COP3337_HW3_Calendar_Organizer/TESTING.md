# HW3 Test Table - Calendar Organizer Phase 3

**How the tests were run:** compiled from the project root with
`javac -d out $(find src -name "*.java")`, then run with
`java -cp out edu.fiu.cop3337.calendar.app.CalendarApp < tests/<file>.txt`.
Relative paths such as `data/input/events.txt` resolve from the project root.
Each `tests/*.txt` file is the exact keyboard input for one run; the dialog
answers (OK / Yes / No / closing the window) are listed in the table.

| # | Group | Input / action | Expected result | Actual result | Pass/Fail |
|---|-------|----------------|-----------------|---------------|-----------|
| 1 | Regression, polymorphism | `tests/01_regression.txt`: `import data/input/events.txt` (8 rows: all 5 valid types + 3 invalid, valid row after a bad one, no blanks) | 5 imported, 3 skipped; each bad row reported with its line number; one summary dialog | Lines 3, 5, 7 skipped with reasons; "Import complete ... 5 imported, 3 skipped."; one INFORMATION_MESSAGE dialog | Pass |
| 2 | Regression | Same run: `print`, `month 11/2026`, `on 11/03/2026`, `owned by ALEX`, `type is task`, `online`, `help` | All HW2 commands work; results sorted; help lists import/export | All match; November 2026 starts on Sunday; `owned by ALEX` finds both "Alex" items; help shows `import filename` / `export filename` | Pass |
| 3 | Polymorphism | Same run: `types` | static=CalendarEvent for every item; EVENT reports dynamic=BasicEvent | `static=CalendarEvent, dynamic=BasicEvent, type=EVENT` plus Task, Meeting, OnlineMeeting, Appointment | Pass |
| 4 | Ordering | `tests/02_validation_and_ordering.txt`: items on different dates; 12:00 am vs 12:00 pm; same start 30 vs 90 min; "Banana Talk" vs "apple Talk" with same date/time/duration; an exact duplicate | Earlier date first; 12:00 am before 12:00 pm; 90 min before 30 min; apple before Banana (ignoring case); both duplicates kept | Printed in exactly that order; both "Duplicate" rows printed | Pass |
| 5 | Validation | Same run: dates 13/45/2026 and 02/29/2025 (not a leap year); times 9:30 am and 13:00 pm | Rejected with a date/time message + ERROR_MESSAGE dialog; re-entry allowed | 4 specific error messages + 4 error dialogs; nothing added | Pass |
| 6 | Validation | Same run: 6 fields and 8 fields for EVENT | Field-count error | "EVENT records need 7 fields, but this one has 6/8." | Pass |
| 7 | Validation | Same run: duration `99999999999`, `abc`, `0`, `-15` | Parsing errors and range errors reported separately | "must be a whole number that fits in an int" (overflow, abc); "must be greater than 0" (0, -15) | Pass |
| 8 | Validation | Same run: invitees `-1` and `3000000000`; priority `0`; completed `yes`; blank meetingLink; blank title; type `PARTY`; blank line; a tab inside a field | Each rejected with a field-specific message | Each produced its own message (e.g. "completed must be true or false, but was \"yes\".") and an error dialog | Pass |
| 9 | Recovery | Same run: valid `TASK ..., 2, TRUE` after all the bad rows | Accepted (boolean ignores case) | Printed as `TASK, Valid After Bad, ..., 2, true` | Pass |
| 10 | Command errors | Same run: `month`, `month 13/2026`, `on`, `on 02/30/2026`, `owned by`, `type is`, `type is PARTY`, `import`, `export   `, `printx` | Helpful console error + one ERROR_MESSAGE dialog each; program keeps running | 10 messages (e.g. "Missing date. Usage: on MM/DD/YYYY") and 10 error dialogs; returned to the command prompt each time | Pass |
| 11 | File boundaries | `tests/03_file_boundaries.txt`: `export data/output/empty_export.txt` with no items | Empty file, 0 written | "0 items written"; file size is 0 bytes | Pass |
| 12 | File boundaries | Same run: `import data/input/empty.txt` | 0 imported, 0 skipped | "0 imported, 0 skipped." | Pass |
| 13 | File boundaries | Same run: `import data/input/blank_line.txt` (valid, blank, valid) | Blank line counts as invalid | "Skipped line 2: ... the record is blank."; 2 imported, 1 skipped | Pass |
| 14 | File boundaries | Same run: `import   data/input/my events/fall schedule.txt  ` (space in the path; field values padded with spaces) | Imports; filename and fields trimmed | 1 imported; printed `Advising Session` and `Student Center Room 2` without extra spaces | Pass |
| 15 | File errors | Same run: `import data/input/missing.txt` | Failure reason + one error dialog, no success dialog | "Import failed: data/input/missing.txt (No such file or directory)" + ERROR_MESSAGE only | Pass |
| 16 | File errors | Same run: `export data/no_such_folder/out.txt` and `export data/output` | Failure reason + one error dialog each | "Export failed: ... (No such file or directory)" and "Export failed: data/output is a folder, not a file." | Pass |
| 17 | Read error | `tests/06_read_error.txt`: `import data/input/read_error.txt` (invalid UTF-8 bytes after about 80 lines) | `Scanner.ioException()` detects the failure; items read so far are kept; "import failed" with partial counts; no success dialog | "Import failed: ... stopped after line 62 (60 imported, 2 skipped before the error; the imported items were kept): MalformedInputException"; one error dialog | Pass |
| 18 | Write error | `export /dev/full` with items loaded, answer Yes (Linux only; `/dev/full` rejects every write) | `PrintWriter.checkError()` after close detects it; no success dialog | "Export failed: Write or close failed." + ERROR_MESSAGE | Pass |
| 19 | Overwrite dialog | `tests/04_overwrite_and_export.txt`: `export data/output/keep_me.txt` (file exists), answer **No** | WARNING_MESSAGE YES_NO confirm; "Export canceled."; file unchanged | Confirm dialog shown; "Export canceled."; SHA-256 of keep_me.txt unchanged | Pass |
| 20 | Overwrite dialog | Same run: export to keep_me.txt again, **close the dialog window** | Treated as cancel; file unchanged | "Export canceled."; SHA-256 unchanged | Pass |
| 21 | Overwrite dialog | `tests/05_restart_and_import.txt`: export to keep_me.txt, answer **Yes** | File replaced; summary dialog with the count | "Export complete: 10 items written to data/output/keep_me.txt."; file now holds the 10 records | Pass |
| 22 | Round trip | Run 04: 5 keyboard items (one per type, with spaces inside fields) + events.txt, then `export data/output/roundtrip.txt`. Restart the program (run 05): `import data/output/roundtrip.txt`, `print`, `types` | Same 10 items with the same type and every field | 10 imported, 0 skipped; `print` output identical line-for-line to the first run; `types` shows the same classes; re-exporting gives a byte-identical file | Pass |

## Dialog evidence

Captured from the program while the tests above ran:

- Error: `tests/screenshots/error_dialog.png` (test 16)
- Confirmation: `tests/screenshots/overwrite_confirmation.png` (test 19)
- Summary: `tests/screenshots/import_summary.png` (test 1)
