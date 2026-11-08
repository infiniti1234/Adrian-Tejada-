# HW3 Test Evidence - Calendar Organizer Phase 3

All tests were run from the `hw3/` folder (so relative paths such as
`data/input/mixed.txt` resolve from there). Full console transcripts are in
`tests/logs/`. Console tests ran with `-Djava.awt.headless=true`, so each
dialog was printed as a `[dialog ...]` line instead of popping up. The real
Swing dialogs were captured on a virtual display (Xvfb) with
`tests/DialogCapture.java` and saved in `tests/screenshots/`.

Data files (real tab characters, UTF-8, no header):

| File | Contents |
|---|---|
| `data/input/mixed.txt` | 8 rows: all 5 valid types + 3 invalid rows (11/31 date, APPOINTMENT missing withWhom, priority 9). Valid rows follow bad rows. |
| `data/input/validation.txt` | 21 rows covering every validation rule (18 bad, 3 good) |
| `data/input/ordering.txt` | 10 EVENT rows that check every sort rule |
| `data/input/blank-lines.txt` | valid row, empty line, spaces-only line, valid row |
| `data/input/empty.txt` | 0 bytes |
| `data/input/my folder/spaced events.txt` | 1 row; the path has spaces in it |

## Test table

| # | Input / action | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|
| **Regression / polymorphism** (`01-regression.txt`) |||||
| 1 | Type one item of each of the 5 types plus a duplicate EVENT, then `Done` | All 6 added, the duplicate included | 6 `Added:` lines | Pass |
| 2 | `help` | Lists all HW#2 commands plus import/export | 11 commands listed | Pass |
| 3 | `print` | All 6, sorted, duplicates kept, subtype fields shown | Sorted 6 rows with invitees/agenda/platform/link/withWhom/priority/completed | Pass |
| 4 | `month 03/2026`, `month 02/2028` | March 2026 starts Sunday; Feb 2028 has 29 days | Correct grids | Pass |
| 5 | `on 03/02/2026` / `on 01/01/2030` | 3 matches sorted / `No events found.` | As expected | Pass |
| 6 | `owned by alex` / `owned by Nobody` | Matches ignore case / `No events found.` | 4 Alex rows / none | Pass |
| 7 | `type is task`, `type is meeting` | Only that type | 1 TASK / 1 MEETING | Pass |
| 8 | `types` | `static=CalendarEvent`; EVENT shows `dynamic=BasicEvent` | `Team Lunch: static=CalendarEvent, dynamic=BasicEvent, type=EVENT` | Pass |
| 9 | `online` | Only the OnlineMeeting, with platform and link (instanceof before downcast) | `Client Sync ... platform: Zoom \| link: https://zoom.us/j/12345` | Pass |
| **Ordering** (`04-ordering.txt`) |||||
| 10 | Import `ordering.txt`, `print` | 12/31/2025 < 05/09/2026 11:59 pm < 05/10/2026 items < 01/01/2027 | Correct date order | Pass |
| 11 | 12:00 am vs 12:00 pm on the same day | Midnight first, noon after 09:00 am | `Midnight` ... `Noon` in that order | Pass |
| 12 | Same start (09:00 am), durations 120 and 15 | Longer first | `Long Same Start` before `Short Same Start` | Pass |
| 13 | Same start and duration, titles `beta tie`, `Alpha tie`, `ALPHA TIE` | Alpha titles (tie, both kept) before `beta tie` | `Alpha tie`, `ALPHA TIE`, `beta tie` | Pass |
| **Validation / recovery: keyboard** (`02-keyboard-validation.txt`) |||||
| 14 | Blank line; `02/29/2026`; `13/01/2026`; `03-01-2026` | Error + ERROR dialog, item not added, re-entry allowed | Specific message for each, item not added | Pass |
| 15 | `02/29/2028` (leap year) | Accepted | Added | Pass |
| 16 | `9:30 am`, `10:75 am` | Bad-time error | `startTime '9:30 am' must use hh:mm am/pm ...` | Pass |
| 17 | 6 fields / 8 fields for EVENT | Field-count error | `EVENT records need 7 fields but this one has 6/8` | Pass |
| 18 | duration `abc`, `2147483648` (int overflow), `0`, `-5` | Parse error (wraps the NumberFormatException) / range error | 2 "must be a whole number that fits in an int" + 2 "must be a positive integer" | Pass |
| 19 | Blank title, lowercase `event`, unknown `PARTY` | Rejected | Blank-field / unknown-type messages | Pass |
| 20 | invitees `-2`, `99999999999`; blank agenda; blank platform; blank withWhom | Rejected with field name | Each message names its field | Pass |
| 21 | priority `0`, `6`; completed `yes`, `1` | Rejected (only true/false is accepted) | Range / boolean messages | Pass |
| 22 | Tab inside a comma-separated field | Rejected (no embedded separators) | `title must not contain commas, tabs, or line breaks` | Pass |
| 23 | Valid TASK with extra spaces and ` TRUE ` after all of the bad rows | Accepted, trimmed | `Added: TASK, Valid After Bad, ..., 3, true` | Pass |
| 24 | `month`, `month 13/2026`, `month 03/abc`, `on`, `on 02/30/2026`, `owned by`, `type is`, `printx`, `hello`, `import`, `import   `, `export` | Error + ERROR dialog, app keeps running | Each gives a helpful error and the next command still runs | Pass |
| **Validation / recovery: file** (`03-import.txt`) |||||
| 25 | `import data/input/mixed.txt` (8 rows) | **5 imported, 3 skipped**; line numbers 3, 5, 7 reported; one summary dialog | `5 imported, 3 skipped`, lines 3/5/7 with reasons, one INFORMATION dialog | Pass |
| 26 | `types` after the import | All five dynamic classes | BasicEvent, Meeting, OnlineMeeting, Appointment, Task | Pass |
| 27 | `import data/input/validation.txt` | 18 bad rows skipped with reasons; 3 good rows (02/29/2028, ` TRUE `, padded fields) imported | `3 imported, 18 skipped` | Pass |
| **File boundaries** |||||
| 28 | `import data/input/empty.txt` | 0 imported, 0 skipped | `0 imported, 0 skipped.` | Pass |
| 29 | `import data/input/blank-lines.txt` | Blank and spaces-only lines count as invalid | `2 imported, 2 skipped` (lines 2, 3) | Pass |
| 30 | `import    data/input/my folder/spaced events.txt   ` | Whole trimmed remainder is the path | `1 imported, 0 skipped` | Pass |
| 31 | Absolute path `import /home/.../hw3/data/input/empty.txt` | Works | Imported 0/0 | Pass |
| 32 | `import data/input/does-not-exist.txt`, `import data/input` (a folder) | `Import failed: ...` + one ERROR dialog, no success dialog | `(No such file or directory)` / `(Is a directory)` | Pass |
| 33 | Export with no items: `export data/output/empty-export.txt` (`05-export.txt` run A) | Empty file, `0 item(s) written` | File is 0 bytes | Pass |
| 34 | `export data/missing-folder/out.txt`, `export data/output` | Export failed + ERROR dialog | `(No such file or directory)` / `is a directory` | Pass |
| 35 | `export data/output/my exports/round trip.txt` when the folder is missing, then again after creating it | Missing parent folder reported; then success | Error, then `5 item(s) written` | Pass |
| 36 | `export /dev/full` with overwrite confirmed (`06-write-error-dev-full.txt`) | `checkError()` after close sees the failed flush; no success message | `Export failed: Write or close failed for /dev/full.` | Pass |
| **Dialogs / round trip** (`05-export.txt` runs B-D, `tests/screenshots/`) |||||
| 37 | Keyboard EVENT + import mixed, `export data/output/round-trip.txt` | 6 sorted TSV lines with real tabs and no header | `cat -A` shows `^I` separators, 6 lines | Pass |
| 38 | Export to the existing file, answer **No** | `Export canceled.`; file byte-for-byte unchanged | Same SHA-256 before and after | Pass |
| 39 | Same with the real Swing dialog (Xvfb), clicking **No** | WARNING YES/NO dialog; file unchanged | `overwrite-confirm.png`; `cmp` identical | Pass |
| 40 | Restart, `import data/output/round-trip.txt`, `print`/`types`/`online`, export again answering **Yes** | Same types and every field; the re-exported file is identical | `6 imported, 0 skipped`; dynamic types match; `cmp` identical | Pass |
| 41 | Screenshots | One error, one confirmation, one summary dialog | `error-invalid-item.png`, `overwrite-confirm.png`, `import-summary.png` (+ welcome/goodbye) | Pass |

## Resource handling notes

* Import: `Scanner` opened in try-with-resources with UTF-8; `Scanner.ioException()`
  is checked after the loop. A read error is reported as `Import failed` with the
  partial counts and a note that the items already imported were kept.
* Export: `PrintWriter(new FileOutputStream(target, false), false, UTF_8)` in
  try-with-resources; `checkError()` is called after close (test 36 shows it
  working on a real device error).
* Overwrite confirmation is shown before any output stream is opened.

## How to reproduce

```bash
cd hw3
javac -d out $(find src -name '*.java')
java -cp out edu.fiu.cop3337.calendar.app.CalendarApp
# headless console test run (dialogs printed as text; confirm answer from a property):
java -Djava.awt.headless=true -Dcalendar.confirmOverwrite=no -cp out edu.fiu.cop3337.calendar.app.CalendarApp < script.txt
```
