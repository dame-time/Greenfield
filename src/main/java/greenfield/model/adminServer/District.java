package greenfield.model.adminServer;

import greenfield.model.robot.CleaningRobot;
import utils.data.DistrictCell;
import utils.data.Position;

import java.util.*;

public class District {
    private final int width;
    private final int height;
    private Map<Integer, List<DistrictCell>> subDistricts;

    public District(int width, int height, int subdivisionSize) throws IllegalArgumentException {
        this.width = width;
        this.height = height;

        buildSubDistricts(subdivisionSize);
    }

    private void buildSubDistricts(int subdivisionFactor) throws IllegalArgumentException {
        if (width % subdivisionFactor != 0 || height % subdivisionFactor != 0)
            throw new IllegalArgumentException("Subdivision must evenly divide both width and height!");

        subDistricts = new HashMap<>();

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

    public Map<Integer, List<DistrictCell>> getSubDistricts() {
        return subDistricts;
    }

    public DistrictCell addRobot(CleaningRobot cleaningRobot) throws Exception {
        int minRobots = Integer.MAX_VALUE;
        int minRobotsSubDistrict = -1;

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

        throw new Exception("Can't find a sub-district where to place the Robot!");
    }
}

