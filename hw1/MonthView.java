/**
 * MonthView represents a 6-row by 7-column calendar view of one month.
 * Columns are Sunday through Saturday. Empty cells contain 0.
 */
public class MonthView {
    private int month;
    private int year;
    private int[][] days;

    /**
     * Constructs a MonthView for a valid month/year.
     * CalendarApp.parseMonthYear(...) must validate month 1-12 and year > 0
     * before this constructor is called.
     */
    public MonthView(int month, int year) {
        this.month = month;
        this.year = year;
        this.days = new int[6][7];
        buildMonth();
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int[][] getDays() {
        return days;
    }

    /**
     * Fills days with the correct day numbers.
     *
     * Steps:
     * 1. Reset all cells to 0.
     * 2. Find the weekday of the first day of the month.
     * 3. Find the number of days in the month.
     * 4. Place 1, 2, 3, ... into the correct cells.
     */
    public void buildMonth() {
        for (int row = 0; row < days.length; row++) {
            for (int col = 0; col < days[row].length; col++) {
                days[row][col] = 0;
            }
        }

        int startCol = CalendarDate.dayOfWeek(month, 1, year);
        int numberOfDays = CalendarDate.daysInMonth(month, year);

        // Cell index counts across rows: cell = row * 7 + col.
        for (int dayNumber = 1; dayNumber <= numberOfDays; dayNumber++) {
            int cell = startCol + dayNumber - 1;
            days[cell / 7][cell % 7] = dayNumber;
        }
    }

    /**
     * Prints:
     * MonthName YEAR
     * Sun Mon Tue Wed Thu Fri Sat
     * then all 6 rows, using blanks for cells containing 0.
     */
    public void printMonth() {
        System.out.println(monthName(month) + " " + year);
        System.out.println("Sun Mon Tue Wed Thu Fri Sat");

        for (int row = 0; row < days.length; row++) {
            String line = "";
            for (int col = 0; col < days[row].length; col++) {
                if (col > 0) {
                    line = line + " ";
                }
                if (days[row][col] == 0) {
                    line = line + "   ";
                } else {
                    line = line + String.format("%3d", days[row][col]);
                }
            }
            System.out.println(line);
        }
    }

    /** Returns January through December for month 1-12. */
    public static String monthName(int month) {
        String[] names = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November",
                          "December"};
        if (month < 1 || month > 12) {
            return "";
        }
        return names[month - 1];
    }
}
