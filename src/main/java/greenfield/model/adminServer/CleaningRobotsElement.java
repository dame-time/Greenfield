package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.DistrictCell;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "robots")
public class CleaningRobotsElement {
    private static final List<CleaningRobot> robots = new ArrayList<>();
    private static final District district = new District(10, 10, 5);

    public static boolean canInsertRobot(CleaningRobot robot) {
        synchronized (robots) {
            return robots.stream().noneMatch(e -> Objects.equals(e.getId(), robot.getId()));
        }
    }

    public static boolean robotExists(String robotID) {
        synchronized (robots) {
            return robots.stream().anyMatch(e -> Objects.equals(e.getId(), robotID));
        }
    }

    public static DistrictCell insertRobot(CleaningRobot robot) throws Exception {
        synchronized (robots) {
            var districtCell = district.addRobot(robot);

            robot.setPosition(districtCell.position);
            robot.setMqttListenerChannel(String.valueOf(districtCell.districtNumber));
            robots.add(robot);

            // TODO: make the robot communicate to others its position

            return districtCell;
        }
    }

    public static List<CleaningRobot> getRobots() {
        synchronized (robots) {
            return new ArrayList<>(robots); // done to prevent to fuck up the reading of the list when returned -> i return a deep copy, so the list does not get concurrently modified.
        }
    }

    public static boolean removeRobot(String robotID) {
        synchronized(robots) {
            if (!robotExists(robotID))
                return false;
            return robots.removeIf(e -> e.getId().equals(robotID));
        }
    }

    public static boolean mutateRobot(String oldRobotIndex, CleaningRobot newRobot) {
        synchronized(robots) {
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
}
