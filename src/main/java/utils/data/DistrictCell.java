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

    public DistrictCell(DistrictCell districtCell) {
        this.position = districtCell.position;
        this.districtNumber = districtCell.districtNumber;
        this.isBusy = districtCell.isBusy;
    }

    public Position<Integer, Integer> getPosition() {
        return position;
    }

    public int getDistrictNumber() {
        return districtNumber;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setPosition(Position<Integer, Integer> position) {
        this.position = position;
    }

    public void setDistrictNumber(int districtNumber) {
        this.districtNumber = districtNumber;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    @Override
    public String toString() {
        return "DistrictCell{" +
                "position=" + position +
                ", isBusy=" + isBusy +
                ", cleaningRobot=" + cleaningRobot +
                ", districtNumber=" + districtNumber +
                '}';
    }
}
