package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.DistrictCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CleaningRobotsElement {
    public static boolean canInsertRobot(CleaningRobot robot) {
        return AdministrationServerRegister.getCleaningRobots().stream().noneMatch(e -> Objects.equals(e.getId(), robot.getId()));
    }

    public static boolean robotExists(String robotID) {
        return AdministrationServerRegister.getCleaningRobots().stream().anyMatch(e -> Objects.equals(e.getId(), robotID));
    }

    public static DistrictCell insertRobot(CleaningRobot robot) throws Exception {
        DistrictCell districtCell;
        districtCell = AdministrationServerRegister.addRobotToDistrict(robot);

        robot.setPosition(districtCell.position);
        robot.setMqttListenerChannel(String.valueOf(districtCell.districtNumber));
        AdministrationServerRegister.addCleaningRobot(robot);

        return districtCell;
    }

    public static List<CleaningRobot> getRobots() {
        return AdministrationServerRegister.getCleaningRobots();
    }

    public static boolean removeRobot(String robotID) {
        if (!robotExists(robotID))
            return false;
        return AdministrationServerRegister.removeRobot(robotID);
    }

    public static boolean mutateRobot(String oldRobotIndex, CleaningRobot newRobot) {
        if (!robotExists(oldRobotIndex))
            return false;
        AdministrationServerRegister.replaceRobot(oldRobotIndex, newRobot);
        return true;
    }
}
