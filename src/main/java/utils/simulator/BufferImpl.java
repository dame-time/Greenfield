package utils.simulator;

import java.util.ArrayList;
import java.util.List;

public class BufferImpl implements Buffer {

    private final List<Measurement> measurements;

    public BufferImpl() {
        measurements = new ArrayList<>();
    }

    @Override
    public void addMeasurement(Measurement m) {
        synchronized (measurements) {
            measurements.add(m);
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {
        synchronized (measurements) {
            var currentMeasurements = new ArrayList<>(measurements); // avoiding the screw-up of my list

            measurements.clear();

            return currentMeasurements;
        }
    }
}
