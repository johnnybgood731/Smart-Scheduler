import java.util.ArrayList;
import java.util.Scanner;
import java.lang.Math;

public class QueryMethods {
    QueryMethods() {
    }

    // Get valid user input about which days of the week can be used for availability for shifts, employees, etc.
    byte getValidDays(String prompt, boolean notBlankFlag) {
        Scanner scan = new Scanner(System.in);
        byte days = 0;
        byte newDayByte;
        String newDay = "";

        while(!newDay.toUpperCase().equals("D")) {
            try {
                System.out.println(prompt + "  Please enter one day at a time.");
                System.out.println("If the shift goes past midnight, enter the day for which the shift begins.");
                System.out.println("(S = Sunday, M = Monday, T = Tuesday, W = Wednesday, R = Thursday, F = Friday," +
                        " Sa = Saturday, D = Done)");
                newDayByte = 0;
                newDay = scan.nextLine().toUpperCase();
                switch (newDay) {
                    case "S":
                        newDayByte = 1;
                        break;
                    case "M":
                        newDayByte = 2;
                        break;
                    case "T":
                        newDayByte = 4;
                        break;
                    case "W":
                        newDayByte = 8;
                        break;
                    case "R":
                        newDayByte = 16;
                        break;
                    case "F":
                        newDayByte = 32;
                        break;
                    case "SA":
                        newDayByte = 64;
                        break;
                    case "D":
                        if (days == 0 && notBlankFlag) {
                            System.out.println("\nYou must include at least 1 day of the week in order to create a " +
                                    "new shift.");
                            newDay = "";
                        }
                        break;
                    default:
                        System.out.println("\nPlease use one of the letter codes described above.");
                }
                if ((days & newDayByte) > 0) {
                    if (newDay.equals("SA")) newDay = "Sa";
                    System.out.println("\nYou have already included " + newDay + ".  Please choose a different day.");
                } else if (newDayByte > 0) {
                    days += newDayByte;
                }
            } catch (java.util.InputMismatchException error) {
                scan.next();
                System.out.println("\nPlease use one of the letter codes described above.");
            }
        }
        return days;
    }

    // Prompts user for input for either creating a new employee or creating a new shift
    void query(String myClass, ArrayList<Employee> employees, ArrayList<Shift> shifts) {
        Scanner scan = new Scanner(System.in);
        NewShift newShift = new NewShift();
        NewEmployee newEmployee = new NewEmployee();

        int newIndex = 0;
        boolean flag = true;
        boolean flag2;
        String answer1;
        String answer2;

        System.out.println("\nPlease create a new " + myClass + " for your schedule.");
        while (flag) {
            if (myClass.equals("shift")) {
                shifts.add(newShift.createNewShift(shifts, employees));
                newIndex = shifts.size() - 1;
                System.out.println("\nYour new shift \"" + shifts.get(newIndex).shiftLabel + "\" starts at " +
                        String.format("%04d", shifts.get(newIndex).shiftStart) + " and ends at " +
                        String.format("%04d", shifts.get(newIndex).shiftEnd) + ".");

                System.out.print("The shift is available on each of the following days: ");
                flag2 = true;
                for (int i = 0; i < 7; i++) {
                    // see Shift.java for an explanation of scheduleDays
                    if ((shifts.get(newIndex).scheduleDays & (byte) Math.pow(2, i)) > 0) {
                        if (flag2) {  // flag2 is true the first time this condition triggers and false otherwise
                            System.out.print(SmartScheduler.GlobalVars.daysOfAWeek[i]);
                            flag2 = false;
                        } else System.out.print(", " + SmartScheduler.GlobalVars.daysOfAWeek[i]);
                    }
                }
                if (shifts.get(newIndex).scheduleDays == 0) System.out.print("None");

                System.out.print("\nThe following employees are able to work \"" + shifts.get(newIndex).shiftLabel +
                        "\": ");
                flag2 = true;
                for (int i : shifts.get(newIndex).availableEmployees) {
                    if (i > 0) {
                        if (flag2) {  // flag2 is true the first time this condition triggers and false otherwise
                            flag2 = false;
                            System.out.print(employees.get(i - 1).fullName);
                        } else System.out.print(", " + employees.get(i - 1).fullName);
                    }
                }
            }

            if (myClass.equals("employee")) {
                employees.add(newEmployee.createNewEmployee(employees));
                newIndex = employees.size() - 1;
                System.out.println("\nYour new employee, " + employees.get(newIndex).fullName +
                        ", has a preference of " + employees.get(newIndex).desiredHours + " hours per week.");
                System.out.print(employees.get(newIndex).fullName + " has requested ");
                flag2 = true;
                for (int i = 0; i < 7; i++) {
                    // see Employee.java for an explanation of requestOff
                    if ((employees.get(newIndex).requestOff & (byte) Math.pow(2, i)) > 0) {
                        if (flag2) {  // flag2 is true the first time this condition triggers and false otherwise
                            System.out.print(SmartScheduler.GlobalVars.daysOfAWeek[i]);
                            flag2 = false;
                        } else System.out.print(", " + SmartScheduler.GlobalVars.daysOfAWeek[i]);
                    }
                }
                if (employees.get(newIndex).requestOff == 0) System.out.print("no days");

                System.out.print(" off and has been approved off for ");

                flag2 = true;
                for (int i = 0; i < 7; i++) {
                    // see Employee.java for an explanation of approvedOff
                    if ((employees.get(newIndex).approvedOff & (byte) Math.pow(2, i)) > 0) {
                        if (flag2) {  // flag2 is true the first time this condition triggers and false otherwise
                            System.out.print(SmartScheduler.GlobalVars.daysOfAWeek[i]);
                            flag2 = false;
                        } else System.out.print(", " + SmartScheduler.GlobalVars.daysOfAWeek[i]);
                    }
                }
                if (employees.get(newIndex).approvedOff == 0) System.out.print("no days");
                System.out.print(".");
            }
            System.out.println("\n\nIs this information correct? (Y/N)");
            answer1 = scan.nextLine().toUpperCase();
            while (!answer1.equals("Y") && !answer1.equals("N") && !answer1.equals("YES") && !answer1.equals("NO")) {
                System.out.println("\nPlease respond with either \"Y\" or \"N\".");
                System.out.println("Is this information correct? (Y/N)");
                answer1 = scan.nextLine().toUpperCase();
            }
            if (answer1.equals("Y") || answer1.equals("YES")) {
                System.out.println("\nWould you like to create another " + myClass + "? (Y/N)");
                answer2 = scan.nextLine().toUpperCase();
                while(!answer2.equals("Y") && !answer2.equals("N") && !answer2.equals("YES") && !answer2.equals("NO")){
                    System.out.println("\nPlease respond with either \"Y\" or \"N\".");
                    System.out.println("Would you like to create another " + myClass + "? (Y/N)");
                    answer2 = scan.nextLine().toUpperCase();
                }
            } else {  // executes if "N" was answered to "Is this information correct?"
                if (myClass.equals("shift")) {
                    shifts.remove(newIndex);
                    if (shifts.size() == 0) flag = false;
                }
                if (myClass.equals("employee")) {
                    employees.remove(newIndex);
                    if (employees.size() == 0) flag = false;
                }
                System.out.print("\n");
                System.out.println("Your entry has been deleted.  Do you still wish to create a new " + myClass +
                        "? (Y/N)");
                answer2 = scan.nextLine().toUpperCase();
                while(!answer2.equals("Y") && !answer2.equals("N") && !answer2.equals("YES") && !answer2.equals("NO")){
                    System.out.println("\nPlease respond with either \"Y\" or \"N\".");
                    System.out.println("Do you still wish to create another " + myClass + "? (Y/N)");
                    answer2 = scan.nextLine().toUpperCase();
                }
            }
            if (answer2.equals("N") || answer2.equals("NO")) {
                if (!flag) System.out.println("\nYou must create at least 1 " + myClass + " in order to continue!");
                flag = !flag;
            } else flag = true;
        }
    }
}