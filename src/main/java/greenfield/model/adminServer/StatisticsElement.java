package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.AirPollutionMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class StatisticsElement {
    public static List<String> getAllRobotsIDInNetwork() {
        return AdministrationServerRegister.getCleaningRobots().stream().map(CleaningRobot::getId).toList();
    }

    public static boolean robotStatsDoesNotExists(String robotID) {
        return AdministrationServerRegister.getAirPollutionStats().values()
                .stream()
                .flatMap(List::stream)
                .map(AirPollutionMeasurement::getRobotID)
                .noneMatch(v -> v.equals(robotID));
    }

    public static boolean robotStatsNumberIsGreaterThan(String robotID, int n) {
        return AdministrationServerRegister.getAirPollutionStats().values()
                .stream()
                .flatMap(List::stream)
                .map(AirPollutionMeasurement::getRobotID)
                .filter(v -> v.equals(robotID))
                .toList()
                .size() >= n;
    }

    public static double getRobotNStats(String robotID, int n) {
        if (robotStatsDoesNotExists(robotID))
            return -1;

        List<Double> stats;

       stats = new ArrayList<>(
                        AdministrationServerRegister.getAirPollutionStats().values()
                           .stream()
                           .flatMap(List::stream)
                           .filter(v -> v.getRobotID().equals(robotID))
                           .toList()
                           .stream()
                           .map(AirPollutionMeasurement::getMeasurement)
                           .toList()
                );

        int iterations = Math.min(stats.size(), n);
        double avg = IntStream.range(0, iterations).mapToDouble(stats::get).sum();

        avg /= iterations;
        return avg;
    }

    public static double getRobotsStatsBetweenTimestamps(long t1, long t2) {
        List<Double> stats;

        stats = AdministrationServerRegister.getAirPollutionStats()
                .values()
                .stream()
                .flatMap(List::stream) // mapping my list of values to a flat List so I can process them accordingly
                .filter(measurement ->
                        measurement.getTimestamp() >= t1 && measurement.getTimestamp() <= t2)
                .map(AirPollutionMeasurement::getMeasurement)
                .toList();

        if (stats.size() < 1)
            return -1;

        int iterations = stats.size();
        double avg = stats.stream().mapToDouble(stat -> stat).sum();

        avg /= iterations;
        return avg;
    }
}
