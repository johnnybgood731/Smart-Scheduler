import java.util.ArrayList;
import java.util.Scanner;
import java.lang.Math;

class NewEmployee {
    NewEmployee() {
    }

    Employee createNewEmployee(ArrayList<Employee> myEmployees) {
        Scanner scan = new Scanner(System.in);
        QueryMethods queryMethod = new QueryMethods();

        int[] tempSchedule = new int[7];
        int[] finalSchedule = new int[7];
        String name = "";

        // Get valid user input to use as the employee's full name
        boolean flag = true;
        while(flag) {
            System.out.println("Enter the employee's full name:");
            name = scan.nextLine();
            if(name.equals("")) {
                System.out.println("\nYou must include a name before continuing.");
                continue;
            }
            if(name.charAt(0) == ' ' || name.charAt(name.length() - 1) == ' ') {
                System.out.println("\nThe employee's name cannot start or end with a space.");
                continue;
            }
            flag = false;
            for(Employee employee : myEmployees) {
                if(employee.fullName.equals(name)) {
                    System.out.println("\nYou already have an employee with this name.");
                    flag = true;
                    break;
                }
            }
        }

        // Get valid user input to use as the number of desired hours per week for creating a new Employee object
        double hours = -1;
        while(hours < 0 || hours > 168) {  // There are only 168 hours in a week
            try {
                System.out.println("How many hours per week would " + name + " like to work?");
                hours = scan.nextFloat();
                if(hours < 0 || hours > 168) {
                    System.out.println("\nPlease enter a number between 0 and 168.  You may enter up to 2 decimals.");
                }
            } catch(java.util.InputMismatchException error) {
                scan.next();
                System.out.println("\nPlease enter a number between 0 and 168.  You may enter up to 2 decimals.");
            }
        }
        hours = Math.round(hours * 100) / 100.0;

        // Get valid user input to use as days requested off and days approved off
        byte reqOff = queryMethod.getValidDays("\nWhich days has " + name + " REQUESTED off?", false);
        byte appOff = queryMethod.getValidDays("\nWhich days has " + name + " BEEN APPROVED to have off?", false);
        for (int i = 0; i < 7; i++) {
            // see Employee.java for an explanation of reqOff
            if ((reqOff & (byte) Math.pow(2, i)) > 0) {
                tempSchedule[i] = -2;
            }
            // see Employee.java for an explanation of appOff
            if ((appOff & (byte) Math.pow(2, i)) > 0) {
                tempSchedule[i] = -1;
                finalSchedule[i] = -1;
            }
        }

        return new Employee(name, hours, reqOff, appOff, tempSchedule, finalSchedule);
    }
}
