package greenfield.controller;

import greenfield.model.robot.CleaningRobot;
import java.util.HashMap;
import java.util.Map;

public class CleaningRobotController {
    private final Map<String, CleaningRobot> robots;

    public CleaningRobotController() {
        robots = new HashMap<>();
    }

    public Map<String, CleaningRobot> getRobots() {
        return robots;
    }

    public void createRobot(int numRobots) {
        for (int i = 0; i < numRobots; i++) {
            var cleaningRobot = new CleaningRobot();
            cleaningRobot.requestToJoinNetwork();
            robots.put(cleaningRobot.getId(), cleaningRobot);
        }
    }

    public void deleteRobot(String robotId) {
        if (robots.get(robotId) != null) {
            robots.get(robotId).disconnectFromServer();
            robots.remove(robotId);
        }
    }

    public void fixRobot(String robotId) {
        if (robots.get(robotId) != null)
            robots.get(robotId).fix();
    }

    public void crashRobot(String robotId) {
        if (robots.get(robotId) != null) {
            robots.get(robotId).crash();
            robots.remove(robotId);
        }
    }
}