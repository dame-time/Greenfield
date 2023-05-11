package utils.data;

import greenfield.model.robot.CleaningRobot;

import java.util.List;
import java.util.Objects;

public final class CleaningRobotHTTPResponse {
    private DistrictCell districtCell;
    private List<CleaningRobot> currentCleaningRobots;

    public CleaningRobotHTTPResponse(DistrictCell districtCell, List<CleaningRobot> currentCleaningRobots) {
        this.districtCell = districtCell;
        this.currentCleaningRobots = currentCleaningRobots;
    }

    public DistrictCell getDistrictCell() {
        return districtCell;
    }

    public List<CleaningRobot> getCurrentCleaningRobots() {
        return currentCleaningRobots;
    }

    public void setDistrictCell(DistrictCell districtCell) {
        this.districtCell = districtCell;
    }

    public void setCurrentCleaningRobots(List<CleaningRobot> currentCleaningRobots) {
        this.currentCleaningRobots = currentCleaningRobots;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CleaningRobotHTTPResponse) obj;
        return Objects.equals(this.districtCell, that.districtCell) &&
                Objects.equals(this.currentCleaningRobots, that.currentCleaningRobots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(districtCell, currentCleaningRobots);
    }

    @Override
    public String toString() {
        return "CleaningRobotHTTPResponse[" +
                "districtCell=" + districtCell + ", " +
                "currentCleaningRobots=" + currentCleaningRobots + ']';
    }
}
