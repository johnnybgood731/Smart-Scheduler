import java.util.Scanner;
import java.util.ArrayList;

class SmartScheduler {
    static class GlobalVars {
        static final String[] daysOfAWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "Saturday"};
    }

    // Iterates n nested for loops to find all possible combinations of size combo of employees
    static void recursion(int n, int combo) {
//        if (n > 0) {
//            for (int i; i < combo; i++) {
//                recursion(n - 1, combo);
//            }
//        }
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

        // Get valid user input to make an array of arrays with
        // { {# of employees on each sunday shift}, {# of employees on each monday shift}, {etc} }.
        int[][] employeesPerShift = new int[7][shifts.size()];
        int dayIndex;
        int shiftIndex = 0;
        int numEmployees;
        int empCount;

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
                    employeesPerShift[dayIndex][shiftIndex] = numEmployees;
                    scan.nextLine();
                }
                dayIndex++;
            }
            shiftIndex++;
        }

        System.out.println("\n\nThe system is now calculating the optimal schedule for you.  Please wait...\n");
    }
}