package utils.data;

import greenfield.model.robot.CleaningRobot;

import java.util.List;

public class CleaningRobotHTTPSetup {
    private DistrictCell districtCell;
    private List<CleaningRobot> currentCleaningRobots;

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
}
