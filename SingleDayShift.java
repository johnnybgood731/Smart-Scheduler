public class SingleDayShift {
    Shift shiftNeeded;
    int dayIndex;
    int employeesNeeded;

    SingleDayShift(Shift shift, int day, int numEmployees) {
        this.shiftNeeded = shift;
        this.dayIndex = day;
        this.employeesNeeded = numEmployees;
    }
}