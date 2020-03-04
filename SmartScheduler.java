// Smart Scheduler is a program which calculates the most efficient possible schedule for any given set of shifts
// and employees.  It calculates all possible combinations of shifts and employees in case of worst-case scenarios,
// but starts by filling in the "least flexible" shift with the "least flexible" employees.

// One thing I would like to add to the functionality in the future is to account for the ability for employees
// to work multiple shifts per day, yet avoid overlapping hours.  I can also check to make sure that one day's
// shifts do not overlap with the next day's shifts on any one employee's schedule.  I may want to consider adding
// a "max hours per day for any employee" option and a "minimum hours between shifts" option.

import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;

class SmartScheduler {
    static class GlobalVars {
        static final String[] daysOfAWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "Saturday"};
        static int minimumFlaws = -1;
        static int targetFlaws = 0;
        static int scheduleCounter = 0;
        static int numSchedules = 0;
    }

    // Calculates the optimal order in which to fill employees into the next shift, and then calls
    // the recursive method "recursion" to fill the employees into the shift in every possible combination.
    static void selectEmployees(int shiftIndex, ArrayList<SingleDayShift> singleDayShifts, ArrayList<Shift> shifts,
                            ArrayList<Employee> employees) {
        int maxMinutes;
        int availableMinutes;
        int counter = 0;
        int desiredMinutes;
        int[] empNumbers = new int[singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees.length];
        int[] empFlexibilities = new int[singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees.length];

        // Finds the number of employees available to work the given shift and creates a list of employee numbers
        // and a list of employee flexibilities. Later, I will sort the employee numbers according to the flexibilities.
        int numAvailable = singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees.length;
        for (int employeeNumber : singleDayShifts.get(shiftIndex).shiftNeeded.availableEmployees) {
            if ((employees.get(employeeNumber - 1).approvedOff & (byte) Math.pow(2, singleDayShifts.get(shiftIndex).dayIndex)) > 0 ||
                    employees.get(employeeNumber - 1).tempSchedule[singleDayShifts.get(shiftIndex).dayIndex] > 0) {
                numAvailable--;  // decrement number of employees available if the employee is off/scheduled for the day
                empFlexibilities[counter] = 20000; // 10,079 is the highest possible flexibility score
            } else {
                desiredMinutes = (int) (employees.get(employeeNumber - 1).desiredHours * 60);
                availableMinutes = 0;
                // Calculates the maximum number of minutes that the employee is available to work for the week
                for (int i = 0; i < 7; i++) {
                    maxMinutes = 0;
                    for (Shift shift : shifts) {
                        // only consider the shift if the employee is one of the shifts's available employees
                        for (int j : shift.availableEmployees) {
                            if (j == employeeNumber) {
                                // only consider the shift if it's offered on day i and employee is not off/scheduled
                                if ((shift.scheduleDays & (byte) Math.pow(2, i)) > 0 &&
                                        employees.get(employeeNumber - 1).tempSchedule[i] == 0) {
                                    if (shift.shiftMinutes > maxMinutes) {
                                        maxMinutes = shift.shiftMinutes;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    availableMinutes += maxMinutes;
                    // if the employee is scheduled for any days, decrease their desiredMinutes appropriately
                    if (employees.get(employeeNumber - 1).tempSchedule[i] > 0) {
                        desiredMinutes -= singleDayShifts.get(employees.get(employeeNumber - 1).tempSchedule[i] - 1).shiftNeeded.shiftMinutes;
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
            employeeSelection[counter] = employees.get(empNumbers[i] - 1);
        }

        recursion(singleDayShifts.get(shiftIndex).employeesNeeded, shiftIndex, singleDayShifts, employeeSelection, 0,
                shifts, employees);
    }

    // Iterates n nested for loops to find all possible combinations of size n of employees
    static void recursion(int n, int shiftIndex, ArrayList<SingleDayShift> singleDayShifts, Employee[] employeeSelection,
                          int recursionStart, ArrayList<Shift> shifts, ArrayList<Employee> employees) {
        if (recursionStart < singleDayShifts.get(shiftIndex).employeesNeeded) {
            for (int i = recursionStart; i <= employeeSelection.length - n; i++) {
                employeeSelection[i].tempSchedule[singleDayShifts.get(shiftIndex).dayIndex] = shiftIndex + 1;
                recursion(n - 1, shiftIndex, singleDayShifts, employeeSelection, i + 1, shifts, employees);
                // If the innermost for loop is currently iterating
                if (recursionStart == singleDayShifts.get(shiftIndex).employeesNeeded - 1) {
                    // If there are still unfilled shifts remaining then move to the next shift and fill with employees
                    if (shiftIndex < singleDayShifts.size() - 1) {
                        // Finds the shift with the lowest flexibility out of the remaining shifts and swaps it into the
                        // next index of singleDayShifts. If any shift is unable to be filled, abandons current schedule.
                        int empCount;
                        int nextShift = shiftIndex + 1;
                        int tempEmpNeed;
                        double tempFlex;
                        double minFlex = -1;
                        boolean flag = true;
                        // Calculates the number of available employees for each of the remaining shifts.
                        for (int j = shiftIndex + 1; j <= singleDayShifts.size() - 1; j++) {
                            empCount = 0;
                            for (int k : singleDayShifts.get(j).shiftNeeded.availableEmployees) {
                                if (employees.get(k - 1).tempSchedule[singleDayShifts.get(j).dayIndex] == 0)
                                    empCount++;
                            }
                            if (empCount - singleDayShifts.get(j).employeesNeeded < 0) {
                                flag = false;  // a false flag fails to move on to the next shift
                                break;
                            } else {
                                // Calculates the flexibility of the shift, which is the number of ways to fill the shift.
                                tempFlex = 1;
                                tempEmpNeed = singleDayShifts.get(j).employeesNeeded;
                                if (tempEmpNeed < empCount - tempEmpNeed) tempEmpNeed = empCount - tempEmpNeed;
                                for (int k = empCount; k > tempEmpNeed; k--)
                                    tempFlex *= k;
                                for (int k = empCount - tempEmpNeed; k > 1; k--)
                                    tempFlex /= k;
                                if (minFlex == -1)
                                    minFlex = tempFlex;  // guaranteed to only happen on the first iteration

                                // Assigns a new value to minFlex if a new minimum has been found
                                if (tempFlex < minFlex) {
                                    minFlex = tempFlex;
                                    nextShift = j;
                                }
                            }
                        }
                        if (flag) {
                            SingleDayShift temp = singleDayShifts.get(shiftIndex + 1);
                            singleDayShifts.set(shiftIndex + 1, singleDayShifts.get(nextShift));
                            singleDayShifts.set(nextShift, temp);
                            selectEmployees(shiftIndex + 1, singleDayShifts, shifts, employees);
                        }
                    }
                    //  if there are no shifts remaining then evaluate how good the current schedule is
                    else {
                        evaluation(employees, singleDayShifts);
                        GlobalVars.numSchedules++;
                    }
                }
                //  resets the most recent tempSchedule entry back to zero in preparation for the next combo
                employeeSelection[i].tempSchedule[singleDayShifts.get(shiftIndex).dayIndex] = 0;
            }
        }
    }

    // Scores a given schedule (one of the Employee fields) based on the number of flaws it has.  A day that
    // was requested off but not given off counts as 2 flaws.  Each hour of overtime counts as a flaw.  Each
    // shift worth of hours over or under a requested number of hours counts as a flaw.  Any flawless shift
    // is printed out to the user, otherwise the system tracks the least flawed "score" via global variables.
    static void evaluation(ArrayList<Employee> employees, ArrayList<SingleDayShift> singleDayShifts) {
        int overtimeFlaws = 0;
        int reqOffFlaws = 0;
        int highHoursFlaws = 0;
        int lowHoursFlaws = 0;
        int deltaHoursFlaws = 0;
        int totalFlaws;
        int minutes;
        int shortestShift = 24*60;
        int maxDeltaHours = -7*24*60;
        int minDeltaHours = 7*24*60;

        for (Employee employee : employees) {
            minutes = 0;
            shortestShift = 24*60;
            // Calculates the length of the shortest shift that the employee works as well as the total minutes that
            // the employee was scheduled for the week. Adds flaws for any requested days off that were not granted.
            for (int i = 0; i < 7; i++) {
                if (employee.tempSchedule[i] > 0) {
                    minutes += singleDayShifts.get(employee.tempSchedule[i] - 1).shiftNeeded.shiftMinutes;
                    if ((employee.requestOff & (byte) Math.pow(2, i)) > 0)
                        reqOffFlaws += 2;
                }
            }

            // Calculates all additional flaws.
            for (SingleDayShift singleDayShift : singleDayShifts) {
                for (int empNumber : singleDayShift.shiftNeeded.availableEmployees) {
                    if (employees.get(empNumber - 1) == employee) {
                        if (singleDayShift.shiftNeeded.shiftMinutes < shortestShift)
                            shortestShift = singleDayShift.shiftNeeded.shiftMinutes;
                        break;
                    }
                }
            }
            if (minutes > 40*60)
                overtimeFlaws += (minutes - 40*60) / 60;
            if ((minutes - (int) (employee.desiredHours*60)) / shortestShift > 0)
                highHoursFlaws += (minutes - (int) (employee.desiredHours*60)) / shortestShift;
            if (minutes < (int) (employee.desiredHours*60))
                lowHoursFlaws += ((int) (employee.desiredHours * 60) - minutes) / shortestShift;
            if (minutes - (int) (employee.desiredHours * 60) > maxDeltaHours)
                maxDeltaHours = minutes - (int) (employee.desiredHours * 60);
            if (minutes - (int) (employee.desiredHours * 60) < minDeltaHours)
                minDeltaHours = minutes - (int) (employee.desiredHours * 60);
        }
        if ((maxDeltaHours - minDeltaHours) / shortestShift > 0)
        deltaHoursFlaws += (maxDeltaHours - minDeltaHours) / shortestShift - 1;
                // minus 1 because the first flaw is not avoidable

        totalFlaws = reqOffFlaws + deltaHoursFlaws + highHoursFlaws + lowHoursFlaws + overtimeFlaws;
        if (totalFlaws == GlobalVars.targetFlaws) {
            printSchedule(employees, singleDayShifts, overtimeFlaws, highHoursFlaws, lowHoursFlaws, deltaHoursFlaws,
                    reqOffFlaws, totalFlaws);
            GlobalVars.scheduleCounter++;
        }
        else if ((GlobalVars.minimumFlaws == -1 || totalFlaws < GlobalVars.minimumFlaws) &&
                totalFlaws > GlobalVars.targetFlaws)
            GlobalVars.minimumFlaws = totalFlaws;
    }

    static void printSchedule(ArrayList<Employee> employees, ArrayList<SingleDayShift> singleDayShifts,
                              int overtime, int highHours, int lowHours, int deltaHours, int reqOff, int total) {
        Scanner scan = new Scanner(System.in);
        String answer;
        int counter;

        for (Employee employee : employees) {
            System.out.println("\n--------------------------------------------------------------------------------\n");
            System.out.println("EMPLOYEE: " + employee.fullName);
            counter = 0;
            for (String day : GlobalVars.daysOfAWeek) {
                if (employee.tempSchedule[counter] == 0) {
                    if ((employee.requestOff & (byte) Math.pow(2, counter)) > 0)
                        System.out.println(day + ": REQUEST OFF");
                    else
                        System.out.println(day + ": OFF");
                }
                else if (employee.tempSchedule[counter] < 0) System.out.println(day + ": PRE-APPROVED OFF");
                else System.out.println(day + ": " + singleDayShifts.get(employee.tempSchedule[counter] - 1).shiftNeeded.shiftLabel);
                counter++;
            }
        }
        System.out.println("\n--------------------------------------------------------------------------------\n");
        System.out.println("SCHEDULE FLAWS:");
        if (total == 0) System.out.println("There are no flaws with this schedule.");
        else {
            if (overtime > 0)
                System.out.println("There were at least " + overtime + " hours of overtime scheduled.");
            if (reqOff > 0)
                System.out.println("There were " + reqOff / 2 + " days requested off, but not given.");
            if (highHours > 0)
                System.out.println("There were at least " + highHours + " shifts worth of extra hours given to " +
                        "employees.");
            if (lowHours > 0)
                System.out.println("There were at least " + lowHours + " shifts worth of hours that employees " +
                        "requested but did not receive.");
            if (deltaHours > 0)
                System.out.println("An employee received at least " + deltaHours + " shifts worth of extra hours " +
                        "than another employee.");
        }
        System.out.println("\nWould you like to keep this schedule? (Y/N)");
        answer = scan.nextLine().toUpperCase();
        while (!answer.equals("Y") && !answer.equals("N") && !answer.equals("YES") && !answer.equals("NO")) {
            System.out.println("\nPlease respond with either \"Y\" or \"N\".");
            System.out.println("Would you like to keep this schedule? (Y/N)");
            answer = scan.nextLine().toUpperCase();
        }
        if (answer.equals("Y") || answer.equals("YES")) {
            System.out.println("\nThank you for using SmartScheduler. Enter anything to exit the program.");
            scan.nextLine();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        ArrayList<Employee> employees = new ArrayList<>();
        ArrayList<Shift> shifts = new ArrayList<>();
        QueryMethods queryMethod = new QueryMethods();
        boolean flag;

        System.out.println("\n\nWelcome to SmartScheduler!  Here, you can find the best possible schedule for your");
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
        int minFlex = employees.size();  // employees.size() is the maximum flexibility score possible and if no shifts
                                         // have a lower score then the program will terminate after line 234 runs

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
                                } else if (empCount - numEmployees < minFlex) {
                                        minFlex = empCount - numEmployees;
                                        shiftIndex = singleDayShifts.size();  // the index that is about to be added
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
                    if (numEmployees > 0) {
                        singleDayShifts.add(new SingleDayShift(shift, dayIndex, numEmployees));
                    } else
                    scan.nextLine();
                }
                dayIndex++;
            }
        }

        if (singleDayShifts.size() == 0) {
            System.out.println("You have chosen not to schedule any employees!");
            System.out.println("Please relaunch the software and try again.");
        } else {
            SingleDayShift temp = singleDayShifts.get(shiftIndex);
            singleDayShifts.set(shiftIndex, singleDayShifts.get(0));
            singleDayShifts.set(0, temp);

            System.out.println("\n\nThe system is now calculating the optimal schedule for you.  Please wait...\n");

            selectEmployees(0, singleDayShifts, shifts, employees);
            while (GlobalVars.scheduleCounter < GlobalVars.numSchedules) {
                GlobalVars.targetFlaws = GlobalVars.minimumFlaws;
                GlobalVars.minimumFlaws = -1;
                GlobalVars.numSchedules = 0;
                selectEmployees(0, singleDayShifts, shifts, employees);
            }
            System.out.print("\nIt is not possible to create a new schedule with the given information.");
            System.out.println("  Please relaunch the program and try again.");
            System.out.println("Enter anything to terminate the program.");
            scan.nextLine();
            scan.nextLine();
        }
    }
}