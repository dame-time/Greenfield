package utils.data;

import greenfield.model.robot.CleaningRobot;

import java.util.List;

public class CleaningRobotHTTPSetup {
    private Position<Integer, Integer> position;
    private List<CleaningRobot> currentCleaningRobots;

    public Position<Integer, Integer> getPosition() {
        return position;
    }

    public List<CleaningRobot> getCurrentCleaningRobots() {
        return currentCleaningRobots;
    }

    public void setPosition(Position<Integer, Integer> position) {
        this.position = position;
    }

    public void setCurrentCleaningRobots(List<CleaningRobot> currentCleaningRobots) {
        this.currentCleaningRobots = currentCleaningRobots;
    }
}
