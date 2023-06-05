package utils.data;

import greenfield.model.robot.CleaningRobot;

import java.util.List;
import java.util.Objects;

public final class CleaningRobotHTTPResponse {
    private DistrictCell districtCell;
    private List<CleaningRobot> currentCleaningRobots;
    private int districtSize;
    private int districtsSubdivisions;

    public CleaningRobotHTTPResponse(
            DistrictCell districtCell,
            List<CleaningRobot> currentCleaningRobots,
            int districtSize,
            int districtsSubdivisions
    ) {
        this.districtCell = districtCell;
        this.currentCleaningRobots = currentCleaningRobots;
        this.districtSize = districtSize;
        this.districtsSubdivisions = districtsSubdivisions;
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
        return "CleaningRobotHTTPResponse{" +
                "districtCell=" + districtCell +
                ", currentCleaningRobots=" + currentCleaningRobots +
                ", districtSize=" + districtSize +
                ", districtsSubdivisions=" + districtsSubdivisions +
                '}';
    }
}
