// Smart Scheduler is a program which calculates the most efficient possible schedule for any given set of shifts
// and employees.  It calculates all possible combinations of shifts and employees in case of worst-case scenarios,
// but starts by filling in the "least flexible" shift with the "least flexible" employees.

// One thing I would like to add to the functionality in the future is to account for the ability for employees
// to work multiple shifts per day, yet avoid overlapping hours.  I can also check to make sure that one day's
// shifts do not overlap with the next day's shifts on any one employee's schedule.  I may want to consider adding
// a "max hours per day for any employee" option and a "minimum hours between shifts" option.

import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.Math;

class SmartScheduler {
    static class GlobalVars {
        static final String[] daysOfAWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "Saturday"};
    }

    // Calculates the optimal order in which to fill employees into the next shift, and then calls
    // the recursive method "recursion" to fill the employees into the shift in every possible combination.
    // Once all of the recursions have finished, selectShift calls "evaluation", and if the schedule is
    // evaluated to be as perfect as possible, it outputs the schedule to the user.  It then gives the user
    // the option of either accepting the schedule or continuing the program.
    static void selectShift(int shiftIndex, ArrayList<SingleDayShift> singleDayShifts, ArrayList<Shift> shifts,
                            ArrayList<Employee> employees) {
        int maxMinutes;
        int availableMinutes = 0;
        int counter = 0;
        int desiredMinutes;
        int[] empNumbers = new int[singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees.length];
        int[] empFlexibilities = new int[singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees.length];

        // Finds the number of employees available to work the given shift and creates a list of employee numbers
        // and a list of employee flexibilities. Later, I will sort the employee numbers according to the flexibilities.
        int numAvailable = singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees[singleDayShifts.get(shiftIndex).dayIndex];
        for (int employeeNumber : singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees) {
            if ((employees.get(employeeNumber).approvedOff & (byte) Math.pow(2, singleDayShifts.get(shiftIndex).dayIndex)) > 0 ||
                    employees.get(employeeNumber).tempSchedule[singleDayShifts.get(shiftIndex).dayIndex] > 0) {
                numAvailable--;  // decrement number of employees available if the employee is off/scheduled for the day
                empFlexibilities[counter] = 20000; // 10,079 is the highest possible flexibility score
            } else {
                desiredMinutes = (int) (employees.get(employeeNumber).desiredHours * 60);
                // Calculates the maximum number of minutes that the employee is available to work for the week
                for (int i = 0; i < 7; i++) {
                    maxMinutes = 0;
                    for (Shift shift : shifts) {
                        // only consider the shift if the employee is one of the shifts's available employees
                        for (int j : shift.availableEmployees) {
                            if (j == employeeNumber) {
                                // only consider the shift if it's offered on day i and employee is not off/scheduled
                                if ((employees.get(employeeNumber).approvedOff & (byte) Math.pow(2, i)) == 0 &&
                                        (shift.scheduleDays & (byte) Math.pow(2, i)) > 0 &&
                                        employees.get(employeeNumber).tempSchedule[i] == 0) {
                                    if (shift.shiftMinutes > maxMinutes) maxMinutes = shift.shiftMinutes;
                                }
                            }
                            break;
                        }
                    }
                    availableMinutes += maxMinutes;
                    // if the employee is scheduled for any days, decrease their desiredMinutes appropriately
                    if (employees.get(employeeNumber).tempSchedule[i] > 0) {
                        desiredMinutes -= singleDayShifts.get(employees.get(employeeNumber).tempSchedule[i]).shiftNeeded.shiftMinutes;
                    }
                }
                empFlexibilities[counter] = availableMinutes - desiredMinutes;
            }
            empNumbers[counter] = employeeNumber;
            counter++;
        }

        //  The only point of making a new array here is to have an array with the smallest possible size for the task.
        //  I could use int[] empNumbers instead, but reducing the number of checks in "recursion" is more important.
        Employee[] employeeSelection = new Employee[numAvailable];

        //  Sorts the non-20000 elements of empNumbers in order of lowest flexibility to highest, and fills the new
        //  array of Employee objects in the same order as the newly sorted empNumbers array.
        for (int i = 0; i < empFlexibilities.length; i++) {
            if (empFlexibilities[i] == 20000) continue;
            counter = 0;  // recycling the counter from earlier in the method
            for (int j = 0; j < empFlexibilities.length; j++) {
                if (empFlexibilities[j] < empFlexibilities[i]) counter++;
                else if (empFlexibilities[j] == empFlexibilities[i] && i > j) counter++;
            }
            employeeSelection[counter] = employees.get(empNumbers[i]);
        }

        recursion(numAvailable, singleDayShifts.get(shiftIndex).employeesNeeded, shiftIndex, singleDayShifts,
                employeeSelection);
//        evaluation();
        // TO DO: ASK USER IF THEY WOULD LIKE TO ACCEPT THE SCHEDULE OR TERMINATE THE PROGRAM
    }

    // Iterates n nested for loops to find all possible combinations of size combo of employees
    static void recursion(int employeesNeeded, int combo, int shiftIndex, ArrayList<SingleDayShift> shifts,
                          Employee[] employeeSelection) {
//        if (employeesNeeded > 0) {
//            for (int i; i < combo; i++) {
//            [FILL EMPLOYEE INTO THE SCHEDULE HERE]
//                recursion(n - 1, combo);
//            }
//        }
        

        // TO DO: SELECT A SHIFT AND SWAP ITS INDEX WITH shiftIndex + 1


//        if (singleDayShifts.size() > shiftIndex + 1) selectShift(shiftIndex + 1, shifts, employees)
    }

    static void evaluation() {

    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        ArrayList<Employee> employees = new ArrayList<>();
        ArrayList<Shift> shifts = new ArrayList<>();
        QueryMethods queryMethod = new QueryMethods();
        boolean flag;

        System.out.println("Welcome to SmartScheduler!  Here, you can find the best possible schedule for your");
        System.out.println("employees.  To get started, you will need to enter some information about your");
        System.out.println("shifts and your employees.");

        queryMethod.query("employee", employees, shifts);
        queryMethod.query("shift", employees, shifts);

        System.out.println("\nYour employees are:");
        for (Employee employee : employees) {
            System.out.println(employee.fullName + " with a preference of " +
                    employee.desiredHours + " hours per week.");
        }
        System.out.println("\nYour shifts are:");
        for (Shift shift : shifts) {
            System.out.print("\"" + shift.shiftLabel + "\" which goes from " +
                    String.format("%04d", shift.shiftStart) + " until " + String.format("%04d", shift.shiftEnd) +
                    " and is available on ");
            flag = true;
            if (shift.scheduleDays == 0) System.out.println("no days");
            else {
                for (int i = 0; i < 7; i++) {
                    // see Shift.java for an explanation of scheduleDays
                    if ((shift.scheduleDays & (byte) Math.pow(2, i)) > 0) {
                        if (flag) {  // flag2 is true the first time this condition triggers and false otherwise
                            System.out.print(GlobalVars.daysOfAWeek[i]);
                            flag = false;
                        } else System.out.print(", " + SmartScheduler.GlobalVars.daysOfAWeek[i]);
                    }
                }
            }
            System.out.println(".");
        }

        // Go through each day for each shift to create new "single day shift" objects and prompt the user for
        // valid input for the number of employees needed for each "single day shift".
        ArrayList<SingleDayShift> singleDayShifts = new ArrayList<>();
        int dayIndex;
        int shiftIndex = 0;
        int numEmployees;
        int empCount;
        int minFlex = -1;

        System.out.println("\n\nNow that you have finished creating shifts and employees, it's time to create a " +
                "schedule.");

        for (Shift shift : shifts) {
            dayIndex = 0;
            for (String day : GlobalVars.daysOfAWeek) {
                // only ask if "shift" is available on "day"
                // see Shift.java for an explanation of scheduleDays
                if ((shift.scheduleDays & (byte) Math.pow(2, dayIndex)) > 0) {
                    // Get valid user input to use as the number of employees needed on any given shift.
                    // Using a do-while loop so that I don't have to reset numEmployees to 0 within a nested for loop.
                    do {
                        try {
                            System.out.println("\nHow many employees need to be scheduled for \"" + shift.shiftLabel +
                                    "\" on " + day + "?");
                            numEmployees = scan.nextInt();
                            if(numEmployees < 0 || numEmployees > employees.size()) {
                                if (employees.size() == 1) System.out.print("\nYou have 1 employee.  ");
                                else System.out.print("\nYou have " + employees.size() + " employees.  ");
                                System.out.println("Please enter a whole number between 0 and " + employees.size() +
                                        ".");
                            } else {
                                // Check to see if the company has enough employees to fill the given shift
                                empCount = 0;
                                for (int num : shift.availableEmployees) {
                                    // if the employee being counted is NOT approved off for the given day
                                    if (num > 0 && (employees.get(num - 1).approvedOff & (byte) Math.pow(2, dayIndex)) == 0) {
                                        empCount++;
                                    }
                                }
                                if (empCount < numEmployees) {
                                    System.out.println("\nYou do not have enough employees available to fill the " +
                                            "shift.");
                                    System.out.println("Please enter a whole number between 0 and " + empCount + ".");
                                    numEmployees = -1;
                                } else {
                                    if (minFlex == -1) minFlex = empCount - numEmployees;
                                    if (empCount - numEmployees < minFlex) {
                                        minFlex = empCount - numEmployees;
                                        shiftIndex = singleDayShifts.size();  // the index that is about to be added
                                    }
                                }
                            }
                        } catch(java.util.InputMismatchException error) {
                            scan.nextLine();
                            System.out.println("\nPlease enter a whole number between 0 and " + employees.size() +
                                    ".");
                            numEmployees = -1;
                        }
                    } while(numEmployees < 0 || numEmployees > employees.size());
                    // this executes once a valid number has been entered
                    if (numEmployees > 0) singleDayShifts.add(new SingleDayShift(shift, dayIndex, numEmployees));
                    scan.nextLine();
                }
                dayIndex++;
            }
        }
        SingleDayShift temp = singleDayShifts.get(shiftIndex);
        singleDayShifts.set(shiftIndex, singleDayShifts.get(0));
        singleDayShifts.set(0, temp);

        System.out.println("\n\nThe system is now calculating the optimal schedule for you.  Please wait...\n");

        selectShift(0, singleDayShifts, shifts, employees);
    }
}