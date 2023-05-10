package utils.data;

import greenfield.model.robot.CleaningRobot;

public class DistrictCell {
    public Position<Integer, Integer> position;
    public boolean isBusy;
    public CleaningRobot cleaningRobot;

    public int districtNumber;

    public DistrictCell(Position<Integer, Integer> position, boolean isBusy, CleaningRobot cleaningRobot, int districtNumber) {
        this.position = position;
        this.isBusy = isBusy;
        this.cleaningRobot = cleaningRobot;
        this.districtNumber = districtNumber;
    }

    public DistrictCell(Position<Integer, Integer> position) {
        this.position = position;
        this.isBusy = false;
        this.cleaningRobot = null;
        this.districtNumber = -1;
    }

    public DistrictCell(Position<Integer, Integer> position, int districtNumber) {
        this.position = position;
        this.isBusy = false;
        this.cleaningRobot = null;
        this.districtNumber = districtNumber;
    }
}
