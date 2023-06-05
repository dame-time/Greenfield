package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.AirPollutionMeasurement;
import utils.data.DistrictCell;

import java.util.*;

public class AdministrationServerRegister {
    private static final Map<String, List<AirPollutionMeasurement>> airPollutionStats = new HashMap<>();
    private static final List<CleaningRobot> robots = new ArrayList<>();
    private static final District district = new District(10, 10, 5);

    public static Map<String, List<AirPollutionMeasurement>> getAirPollutionStats() {
        synchronized (airPollutionStats) {
            return new HashMap<>(airPollutionStats);
        }
    }

    public static List<CleaningRobot> getCleaningRobots() {
        synchronized (robots) {
            return new ArrayList<>(robots);
        }
    }

    public static District getDistricts() {
        synchronized (district) {
            return district;
        }
    }

    public static List<AirPollutionMeasurement> putAirPollutionStats(String key, List<AirPollutionMeasurement> measurements) {
        synchronized (airPollutionStats) {
            return airPollutionStats.put(key, measurements);
        }
    }

    public static boolean addCleaningRobot(CleaningRobot robot) {
        synchronized (robots) {
            return robots.add(robot);
        }
    }

    public static boolean removeRobot(String robotID) {
        synchronized (robots) {
            if (robots.removeIf(e -> e.getId().equals(robotID)))
                return removeRobotFromDistrict(robotID);
        }

        return false;
    }

    public static void replaceRobot(String oldRobotIndex, CleaningRobot newRobot) {
        synchronized (robots) {
            robots.replaceAll(e -> {
                if (Objects.equals(e.getId(), oldRobotIndex))
                    return newRobot;
                return e;
            });
        }
    }

    public static DistrictCell addRobotToDistrict(CleaningRobot robot) throws Exception {
        synchronized (district) {
            return district.addRobot(robot);
        }
    }

    public static boolean removeRobotFromDistrict(String robotID) {
        synchronized (district) {
            return district.removeRobot(robotID);
        }
    }
}
