package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.DistrictCell;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "robots")
public class CleaningRobotsElement {
    private static List<CleaningRobot> robots = new ArrayList<>();
    private static District district = new District(10, 10, 5);

    public static boolean canInsertRobot(CleaningRobot robot) {
        return robots.stream().filter(e -> Objects.equals(e.getId(), robot.getId())).count() < 1;
    }

    public static boolean robotExists(String robotID) {
        return robots.stream().filter(e -> Objects.equals(e.getId(), robotID)).count() > 0;
    }

    public static DistrictCell insertRobot(CleaningRobot robot) throws Exception {
        var districtCell = district.addRobot(robot);

        robot.setPosition(districtCell.position);
        robot.setMqttListenerChannel(String.valueOf(districtCell.districtNumber));
        robots.add(robot);

        // TODO: make the robot communicate to others its position

        return districtCell;
    }

    public static List<CleaningRobot> getRobots() {
        return robots;
    }

    public static boolean removeRobot(String robotID) {
        if (!robotExists(robotID))
            return false;

        return robots.removeIf(e -> e.getId().equals(robotID));
    }

    public static boolean mutateRobot(String oldRobotIndex, CleaningRobot newRobot) {
        if (!robotExists(oldRobotIndex))
            return false;

        robots.replaceAll(e -> {
            if (Objects.equals(e.getId(), oldRobotIndex))
                return newRobot;

            return e;
        });
        return true;
    }
}
