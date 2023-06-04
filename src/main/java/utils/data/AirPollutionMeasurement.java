package utils.data;

import java.util.List;

public class AirPollutionMeasurement {
    private Double measurement;
    private long timestamp;
    private String robotID;

    public AirPollutionMeasurement(Double measurement, long timestamp, String robotID) {
        this.measurement = measurement;
        this.timestamp = timestamp;
        this.robotID = robotID;
    }

    public Double getMeasurement() {
        return measurement;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getRobotID() {
        return robotID;
    }

    public void setMeasurement(Double measurement) {
        this.measurement = measurement;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRobotID(String robotID) {
        this.robotID = robotID;
    }

    @Override
    public String toString() {
        return "AirPollutionMeasurement{" +
                "measurement=" + measurement +
                ", timestamp=" + timestamp +
                ", robotID=" + robotID +
                '}';
    }
}
