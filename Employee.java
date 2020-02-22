class Employee {
    // approvedOff and requestOff are bytes with a 1 in the position of any day that is either requested or approved
    // off, where Sunday is the 1s place, Monday is the 2s place, etc.

    String fullName;
    double desiredHours;
    byte requestOff;
    byte approvedOff;
    int[] tempSchedule;
    int[] finalSchedule;

    Employee(String name, double hours, byte reqOff, byte appOff, int[] tentativeSchedule, int[] schedule) {
        this.fullName = name;
        this.desiredHours = hours;
        this.requestOff = reqOff;
        this.approvedOff = appOff;
        this.tempSchedule = tentativeSchedule;
        this.finalSchedule = schedule;
    }
}