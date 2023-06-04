package utils.data;

public class StatisticsHTTPSetup {
    private double averageAirPollution;
    private StatisticsResponseStatus responseStatus;
    private String warningMessage;
    private String errorMessage;

    public double getAverageAirPollution() {
        return averageAirPollution;
    }

    public void setAverageAirPollution(double averageAirPollution) {
        this.averageAirPollution = averageAirPollution;
    }

    public StatisticsResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(StatisticsResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
