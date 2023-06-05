package utils.data;

import greenfield.model.robot.CleaningRobot;

import java.util.List;

public class CleaningRobotHTTPSetup {
    private DistrictCell districtCell;
    private List<CleaningRobot> currentCleaningRobots;
    private int districtSize;
    private int districtsSubdivisions;

    public CleaningRobotHTTPSetup() {

    }

    public DistrictCell getDistrictCell() {
        return districtCell;
    }

    public List<CleaningRobot> getCurrentCleaningRobots() {
        return currentCleaningRobots;
    }

    public int getDistrictSize() {
        return districtSize;
    }

    public int getDistrictsSubdivisions() {
        return districtsSubdivisions;
    }

    public void setDistrictCell(DistrictCell districtCell) {
        this.districtCell = districtCell;
    }

    public void setCurrentCleaningRobots(List<CleaningRobot> currentCleaningRobots) {
        this.currentCleaningRobots = currentCleaningRobots;
    }

    public void setDistrictSize(int districtSize) {
        this.districtSize = districtSize;
    }

    public void setDistrictsSubdivisions(int districtsSubdivisions) {
        this.districtsSubdivisions = districtsSubdivisions;
    }

    @Override
    public String toString() {
        return "CleaningRobotHTTPSetup{" +
                "districtCell=" + districtCell +
                ", currentCleaningRobots=" + currentCleaningRobots +
                ", districtSize=" + districtSize +
                ", districtSubdivisions=" + districtsSubdivisions +
                '}';
    }
}
