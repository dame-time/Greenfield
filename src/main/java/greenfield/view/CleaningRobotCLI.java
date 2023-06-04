package greenfield.view;

import greenfield.model.robot.CleaningRobot;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CleaningRobotCLI {
    private static final Map<String, CleaningRobot> robots = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("\033[1;33m" + "Welcome to the Cleaning Robot CLI! Please use the following commands:" + "\033[0m");
        System.out.println("\033[1;32m" + "print - To print all current robots" + "\033[0m");
        System.out.println("\033[1;32m" + "create - To create a new robot" + "\033[0m");
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
                        robots.values().stream().map(CleaningRobot::getId).toList() + "\033[0m");
                case "create" -> {
                    var clearingRobot = new CleaningRobot();
                    clearingRobot.requestToJoinNetwork();
                    robots.put(clearingRobot.getId(), clearingRobot);
                }
                case "delete" -> {
                    if (robots.get(robotId) != null) {
                        robots.get(robotId).disconnectFromServer();
                        robots.remove(robotId);
                    } else
                        System.out.println("\033[0;31m" + "No robot found with ID: " + robotId + "\033[0m");
                }
                case "fix" -> {
                    if (robots.get(robotId) != null)
                        robots.get(robotId).fix();
                    else
                        System.out.println("\033[0;31m" + "No robot found with ID: " + robotId + "\033[0m");
                }
                case "crash" -> {
                    if (robots.get(robotId) != null) {
                        robots.get(robotId).crash();
                        robots.remove(robotId);
                    } else
                        System.out.println("\033[0;31m" + "No robot found with ID: " + robotId + "\033[0m");
                }
                default -> System.out.println("\033[0;31m" + "Invalid command, please try again." + "\033[0m");
            }
        }

        System.out.println("\033[0;34m" + "Terminating CLI. Goodbye!" + "\033[0m");
    }
}
