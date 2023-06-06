package greenfield.view;

import greenfield.controller.AdministrationClientController;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class AdministrationClient {
    private static final AdministrationClientController controller = new AdministrationClientController();

    public static void main(String[] args) throws IOException {
        System.out.println("\033[1;33m" + "Welcome to the Administration Client! Please use the following commands:" + "\033[0m");
        System.out.println("\033[1;32m" + "robots - To get all robot IDs" + "\033[0m");
        System.out.println("\033[1;32m" + "<robotID> <n> - To get stats for robot with ID <robotID> for last <n> measurements" + "\033[0m");
        System.out.println("\033[1;32m" + "<t1> <t2> - To get stats between timestamps <t1> and <t2>" + "\033[0m");
        System.out.println("\033[1;32m" + "q - To terminate the client" + "\033[0m");

        Scanner scanner = new Scanner(System.in);

        boolean hasUserPressedEsc = false;

        while (!hasUserPressedEsc) {
            System.out.print("\033[1;36m" + "Enter command: " + "\033[0m");
            String command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (command.equalsIgnoreCase("q")) {
                System.out.println("\033[0;34m" + "Terminating client. Goodbye!" + "\033[0m");
                hasUserPressedEsc = true;
                continue;
            }

            if (parts.length == 1 && parts[0].equalsIgnoreCase("robots")) {
                List<String> robotIDs = controller.getAllRobotIDs();
                System.out.println("\033[0;34m" + "Robot IDs: " + robotIDs + "\033[0m");
            } else if (parts.length == 2 && isInteger(parts[1])) {
                var stats = controller.getRobotStats(parts[0], Integer.parseInt(parts[1])).getAverageAirPollution();
                System.out.println("\033[0;35m" + "Stats for robot " + parts[0] + ": " + stats + "\033[0m");
            } else if (parts.length == 2 && isLong(parts[0]) && isLong(parts[1])) {
                var stats = controller.getRobotsStatsBetweenTimestamps(Long.parseLong(parts[0]), Long.parseLong(parts[1])).getAverageAirPollution();
                System.out.println("\033[0;35m" + "Stats between timestamps " + parts[0] + " and " + parts[1] + ": " + stats + "\033[0m");
            } else {
                System.out.println("\033[0;31m" + "Invalid command, please try again." + "\033[0m");
            }
        }
    }

    private static boolean isInteger(String strNum) {
        if (strNum == null)
            return false;

        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    private static boolean isLong(String strNum) {
        if (strNum == null)
            return false;

        try {
            Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
}
