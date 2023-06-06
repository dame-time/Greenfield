package greenfield.view;

import greenfield.controller.CleaningRobotController;
import greenfield.model.robot.CleaningRobot;

import java.util.Scanner;

public class CleaningRobotCLI {
    private static final CleaningRobotController controller = new CleaningRobotController();

    public static void main(String[] args) {
        System.out.println("\033[1;33m" + "Welcome to the Cleaning Robot CLI! Please use the following commands:" + "\033[0m");
        System.out.println("\033[1;32m" + "print - To print all current robots" + "\033[0m");
        System.out.println("\033[1;32m" + "create <n> - To create n new robots (by default one)" + "\033[0m");
        System.out.println("\033[1;32m" + "delete <robotID> - To delete a robot" + "\033[0m");
        System.out.println("\033[1;32m" + "fix <robotID> - To fix a robot" + "\033[0m");
        System.out.println("\033[1;32m" + "crash <robotID> - To crash a robot" + "\033[0m");
        System.out.println("\033[1;32m" + "q - To quit the CLI" + "\033[0m");

        Scanner scanner = new Scanner(System.in);
        String command;

        boolean hasUserPressedExitButton = false;

        while (!hasUserPressedExitButton) {
            System.out.print("\033[1;36m" + "Enter command: " + "\033[0m");
            command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (command.equalsIgnoreCase("q")) {
                hasUserPressedExitButton = true;
                continue;
            }

            String robotId = "";
            if (parts.length > 1)
                robotId = parts[1];

            switch (parts[0].toLowerCase()) {
                case "print" -> System.out.println("\033[1;32m" + "Current Robots connected: " +
                        controller.getRobots().values().stream().map(CleaningRobot::getId).toList() + "\033[0m");
                case "create" -> {
                    int numRobots = 1;
                    if (parts.length > 1) {
                        try {
                            numRobots = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            System.out.println("\033[0;31m" + "Invalid number of robots. Creating one robot." + "\033[0m");
                        }
                    }
                    controller.createRobot(numRobots);
                }
                case "delete" -> controller.deleteRobot(robotId);
                case "fix" -> controller.fixRobot(robotId);
                case "crash" -> controller.crashRobot(robotId);
                default -> System.out.println("\033[0;31m" + "Invalid command, please try again." + "\033[0m");
            }
        }

        System.out.println("\033[0;34m" + "Terminating CLI. Goodbye!" + "\033[0m");
    }
}
