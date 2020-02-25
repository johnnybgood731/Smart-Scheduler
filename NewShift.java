import java.util.ArrayList;
import java.util.Scanner;

public class NewShift {
    NewShift() {
    }

    Shift createNewShift(ArrayList<Shift> myShifts, ArrayList<Employee> myEmployees) {
        Scanner scan = new Scanner(System.in);
        QueryMethods queryMethod = new QueryMethods();

        // Get valid user input to use as a start time for creating a new Shift object
        int start = -1;
        while(start < 0 || start > 2399) {
            try {
                System.out.println("Enter shift start time using military time:");
                start = scan.nextInt();
                if(start < 0 || start > 2399) {
                    System.out.println("\nPlease enter a whole number between 0000 and 2359.");
                }
            } catch(java.util.InputMismatchException error) {
                scan.next();
                System.out.println("\nPlease enter a valid start time.");
            }
        }

        // if the military time entered has more than 59 minutes, overflow the minutes into the next hour
        if(start % 100 > 59) {
            start += 40;
        }

        // Get valid user input to use as an end time for creating a new Shift object
        int end = -1;
        while(end < 0 || end > 2359 || end == start || (end % 100 > 59 && end == start - 40)) {
            try {
                System.out.println("Enter shift end time using military time:");
                end = scan.nextInt();
                if(end < 0 || end > 2399) {
                    System.out.println("\nPlease enter a whole number between 0000 and 2359.");
                }
                if(end == start || (end % 100 > 59 && end == start - 40)) {
                    System.out.println("\nThe end time must be different from the start time.");
                    end = -1;
                }
            } catch(java.util.InputMismatchException error) {
                scan.next();
                System.out.println("\nPlease enter a valid end time.");
                System.out.println("Enter shift end time using military time:");
            }
        }

        // if the military time entered has more than 59 minutes, overflow the minutes into the next hour
        if(end % 100 > 59) {
            end += 40;
        }

        // Calculate how many minutes are in the shift.  I could calculate it in the calculateAvailableHours method,
        // but I would either have to calculate it 7n times or be unable to efficiently find the shortest shift per day
        int minutes;
        minutes = (end % 100) - (start % 100);
        minutes += (end / 100 - start / 100) * 60;
        if (minutes < 0) minutes += 24 * 60;


        // Get valid user input to use as the days of the week for which the shift is available
        byte days = queryMethod.getValidDays("\nWhich days of the week will this shift be available?", true);
        scan.nextLine();

        // Get valid user input to use as a label for the shift
        String label = "";
        boolean flag = true;
        while(flag) {
            System.out.println("\nEnter a name for your shift:");
            label = scan.nextLine();
            flag = false;
            for(Shift shift : myShifts) {
                if(shift.shiftLabel.equals(label)) {
                    flag = true;
                    System.out.println("\nYou have already used this name.  Please choose a different name.");
                    break;
                }
            }
        }

        // Get valid user input to use as an ArrayList storing the indices of which employees can work the shift
        int[] employees = new int[myEmployees.size()];
        int newEmp = 1;
        int counter;
        flag = true;        // recycling the flag from earlier in the method

        while(newEmp != 0) {
            try {
                counter = 0;
                System.out.println("\nYour employees are:");
                for (Employee employee : myEmployees) {
                    counter++;
                    System.out.println(counter + ". " + employee.fullName);
                }
                System.out.println("\nWhich employees can work \"" + label + "\"?  Please enter only the number" +
                        " of the employee and enter only one employee at a time.  Enter \"0\" when finished.");
                newEmp = scan.nextInt();
                if(newEmp < 0 || newEmp > myEmployees.size()) {
                    System.out.println("\nPlease enter one of the numbers listed above.");
                } else if(newEmp == 0 && flag) {
                    System.out.println("\nYou must include at least 1 employee in order to create a new shift.");
                    newEmp = 1;
                } else if(newEmp > 0) {
                    if(employees[newEmp - 1] == newEmp) {  // shifts[newShift - 1] is set in line 104
                        System.out.println("\nYou have already included employee #" + newEmp + ". Please choose " +
                                "another employee or enter \"0\" to finish assigning employees to \"" + label + "\".");
                    } else {
                        employees[newEmp - 1] = newEmp;
                        flag = false;
                    }
                }
            } catch(java.util.InputMismatchException error) {
                scan.next();
                System.out.println("\nPlease enter one of the numbers listed above.");
            }
        }

        return new Shift(start, end, minutes, days, label, employees);
    }
}
