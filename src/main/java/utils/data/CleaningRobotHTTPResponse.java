package utils.data;

import greenfield.model.robot.CleaningRobot;

import java.util.List;
import java.util.Objects;

public final class CleaningRobotHTTPResponse {
    private Position<Integer, Integer> position;
    private List<CleaningRobot> currentCleaningRobots;

    public CleaningRobotHTTPResponse(Position<Integer, Integer> position, List<CleaningRobot> currentCleaningRobots) {
        this.position = position;
        this.currentCleaningRobots = currentCleaningRobots;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CleaningRobotHTTPResponse) obj;
        return Objects.equals(this.position, that.position) &&
                Objects.equals(this.currentCleaningRobots, that.currentCleaningRobots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, currentCleaningRobots);
    }

    @Override
    public String toString() {
        return "CleaningRobotHTTPResponse[" +
                "position=" + position + ", " +
                "currentCleaningRobots=" + currentCleaningRobots + ']';
    }
}
