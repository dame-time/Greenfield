package greenfield.model.robot;

public class AirPollutionSensor {
    private double pm10;
    private long timestamp;

    public AirPollutionSensor() {
        this.pm10 = 0;
        this.timestamp = 0;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getPm10() {
        return pm10;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPm10(double pm10) {
        this.pm10 = pm10;
    }
}
