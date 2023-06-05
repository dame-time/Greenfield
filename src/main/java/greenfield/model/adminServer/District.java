package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.DistrictCell;
import utils.data.Pair;
import utils.data.Position;

import java.util.*;
import java.util.stream.Collectors;

public class District {
    private final int width;
    private final int height;
    private final int subdivisions;
    private final Map<Integer, List<DistrictCell>> subDistricts;

    public District(int width, int height, int subdivisionSize) throws IllegalArgumentException {
        this.width = width;
        this.height = height;

        this.subdivisions = subdivisionSize;
        this.subDistricts = new HashMap<>();

        buildSubDistricts(subdivisionSize);
    }

    public District(District district) {
        this.width = district.width;
        this.height = district.height;

        this.subdivisions = district.subdivisions;
        this.subDistricts = district.subDistricts;
    }

    private void buildSubDistricts(int subdivisionFactor) throws IllegalArgumentException {
        if (width % subdivisionFactor != 0 || height % subdivisionFactor != 0)
            throw new IllegalArgumentException("Subdivision must evenly divide both width and height!");

        int subDistrictsWidth = width / subdivisionFactor;
        int subDistrictsHeight = height / subdivisionFactor;

        for (int i = 0; i < subDistrictsWidth; i++) {
            for (int j = 0; j < subDistrictsHeight; j++) {
                List<DistrictCell> districtCells = new ArrayList<>();

                int subDistrictNumber = i * subDistrictsHeight + j; // flattening the district number -> basically the number of the district

                for (int x = i * subdivisionFactor; x < (i + 1) * subdivisionFactor; x++) {
                    for (int y = j * subdivisionFactor; y < (j + 1) * subdivisionFactor; y++) {
                        // Add the current position to the list
                        Position<Integer, Integer> cellPosition = new Position<>(x, y);
                        districtCells.add(new DistrictCell(cellPosition, subDistrictNumber));
                    }
                }

                subDistricts.put(subDistrictNumber, districtCells);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSubdivisions() {
        return subdivisions;
    }

    public Map<Integer, List<DistrictCell>> getSubDistricts() {
        synchronized (subDistricts) {
            return subDistricts;
        }
    }

    public DistrictCell addRobot(CleaningRobot cleaningRobot) throws Exception {
        int minRobots = Integer.MAX_VALUE;
        int minRobotsSubDistrict = -1;

        synchronized (subDistricts) {
            for (var entry : subDistricts.entrySet()) {
                int robotCount = (int) entry.getValue().stream().filter(cell -> cell.isBusy).count();

                if (robotCount < minRobots) {
                    minRobots = robotCount;
                    minRobotsSubDistrict = entry.getKey();
                }
            }

            if (minRobotsSubDistrict != -1) {
                // Even if we find a supposed free cell, we check that its effectively free, if it's not I have no free cells
                for (var cell : subDistricts.get(minRobotsSubDistrict)) {
                    if (cell.cleaningRobot == null) {
                        cell.cleaningRobot = cleaningRobot;
                        cell.isBusy = true;

                        return cell;
                    }
                }
                throw new Exception("All the cells are full, can't add a new Robot!");
            }
        }

        throw new Exception("Can't find a sub-district where to place the Robot!");
    }

    public boolean placeRobotAtPosition(Position<Integer, Integer> position) {
        synchronized (subDistricts) {
            for (List<DistrictCell> districtCells : subDistricts.values()) {
                for (DistrictCell cell : districtCells) {
                    if (cell.getPosition().equals(position) && !cell.isBusy) {
                        cell.isBusy = true;

                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Pair<DistrictCell, DistrictCell> findCellsToBalance() {
        int maxRobotsSubDistrict = -1;
        int minRobotsSubDistrict = -1;

        int maxRobots = -1;
        int minRobots = Integer.MAX_VALUE;

        for (var entry : subDistricts.entrySet()) {
            int robotCount = (int) entry.getValue().stream().filter(cell -> cell.isBusy).count();

            if (robotCount > maxRobots) {
                maxRobots = robotCount;
                maxRobotsSubDistrict = entry.getKey();
            }

            if (robotCount < minRobots) {
                minRobots = robotCount;
                minRobotsSubDistrict = entry.getKey();
            }
        }

//        System.out.println("Max robots: " + maxRobots + ", Min robots: " + minRobots);

        if (maxRobots - minRobots > 1) {
            DistrictCell maxCell = subDistricts.get(maxRobotsSubDistrict).stream()
                    .filter(cell -> cell.isBusy)
                    .findFirst()
                    .orElse(null);
            DistrictCell minCell = subDistricts.get(minRobotsSubDistrict).stream()
                    .filter(cell -> !cell.isBusy)
                    .findFirst()
                    .orElse(null);

            if (maxCell != null && minCell != null) {
//                System.out.println("Found cells to balance: " + maxCell + " and " + minCell);
                return new Pair<>(maxCell, minCell);
            }
        }

//        System.out.println("Did not find any cells to balance.");
        return null;
    }

    public boolean removeRobot(String robotId) {
        synchronized (subDistricts) {
            for (List<DistrictCell> districtCells : subDistricts.values()) {
                for (DistrictCell cell : districtCells) {
                    if (cell.cleaningRobot != null && cell.cleaningRobot.getId().equals(robotId)) {
                        cell.cleaningRobot = null;
                        cell.isBusy = false;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean removeRobotFromPosition(Position<Integer, Integer> position) {
        synchronized (subDistricts) {
            for (List<DistrictCell> districtCells : subDistricts.values()) {
                for (DistrictCell cell : districtCells) {
                    if (cell.getPosition().equals(position) && cell.isBusy) {
//                        System.err.println("Removing robot from position: " + position);
                        cell.cleaningRobot = null;
                        cell.isBusy = false;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public int getTotalNumberOfRobotsInDistricts() {
        synchronized (subDistricts) {
            return (int) subDistricts.values().stream()
                    .flatMap(List::stream)
                    .filter(DistrictCell::isBusy)
                    .count();
        }
    }

    public boolean checkIfCellIsBusy(Position<Integer, Integer> position) {
        synchronized (subDistricts) {
            for (List<DistrictCell> districtCells : subDistricts.values())
                for (DistrictCell cell : districtCells)
                    if (cell.getPosition().equals(position))
                        return cell.isBusy;
        }

        return false;
    }


    public List<Integer> getRobotsPerDistrict() {
        List<Integer> robotCount = new ArrayList<>();

        synchronized (subDistricts) {
            for (var entry : subDistricts.entrySet()) {
                int counter = (int) entry.getValue().stream().filter(cell -> cell.isBusy).count();
                robotCount.add(counter);
            }
        }

        return robotCount;
    }

    @Override
    public String toString() {
        return "District{" +
                "width=" + width +
                ", height=" + height +
                ", subdivisions=" + subdivisions +
                ", subDistricts=" + getSubDistricts() +
                '}';
    }
}

