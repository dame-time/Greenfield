package utils.data;

public class StatisticsHTTPResponse {
    private final double averageAirPollution;
    private final StatisticsResponseStatus responseStatus;
    private final String warningMessage;
    private final String errorMessage;

    private StatisticsHTTPResponse(Builder builder) {
        this.averageAirPollution = builder.averageAirPollution;
        this.responseStatus = builder.responseStatus;
        this.warningMessage = builder.warningMessage;
        this.errorMessage = builder.errorMessage;
    }

    public double getAverageAirPollution() {
        return averageAirPollution;
    }

    public StatisticsResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private double averageAirPollution;
        private StatisticsResponseStatus responseStatus;
        private String warningMessage;
        private String errorMessage;

        private Builder() {
            this.averageAirPollution = -1;
            this.responseStatus = StatisticsResponseStatus.COMPLETED;
            this.warningMessage = "";
            this.errorMessage = "";
        }

        public Builder setAverageAirPollution(double averageAirPollution) {
            this.averageAirPollution = averageAirPollution;
            return this;
        }

        public Builder setResponseStatus(StatisticsResponseStatus responseStatus) {
            this.responseStatus = responseStatus;
            return this;
        }

        public Builder setWarningMessage(String warningMessage) {
            this.warningMessage = warningMessage;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public StatisticsHTTPResponse build() {
            return new StatisticsHTTPResponse(this);
        }
    }
}
