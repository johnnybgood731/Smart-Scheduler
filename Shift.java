class Shift {
    // scheduleDays is a byte with a 1 in the position of any day that is able to be scheduled, where
    // Sunday is the 1s place, Monday is the 2s place, etc.

    int shiftStart;
    int shiftEnd;
    int shiftMinutes;
    byte scheduleDays;
    String shiftLabel;
    int[] availableEmployees;

    Shift(int start, int end, int minutes, byte days, String label, int[] availEmployees) {
        this.shiftStart = start;
        this.shiftEnd = end;
        this.shiftMinutes = minutes;
        this.scheduleDays = days;
        this.shiftLabel = label;
        this.availableEmployees = availEmployees;
    }
}