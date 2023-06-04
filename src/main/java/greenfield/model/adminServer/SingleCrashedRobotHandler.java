package greenfield.model.adminServer;

import utils.data.AirPollutionMeasurement;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SingleCrashedRobotHandler extends Thread {
    private String singleProcessName;
    private long lastMessageTimestamp;

    public SingleCrashedRobotHandler() {
        lastMessageTimestamp = 0;
        singleProcessName = "";
    }

    public synchronized void shutDownSingleProcessCrashHandler() {
        this.interrupt();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (AdministrationServerRegister.getCleaningRobots().size() != 1) {
                singleProcessName = "";
                lastMessageTimestamp = 0;
                waitNextIteration(15000);
                continue;
            }

            singleProcessName = getLastRobotID();
            lastMessageTimestamp = getLastMessageTimestamp();

            waitNextIteration(30000);

            System.out.println("\033[1;33m" +
                    "diff timestamp: " + (lastMessageTimestamp - getLastMessageTimestamp()) + "\033[0m");

            if (lastMessageTimestamp - getLastMessageTimestamp() >= 0){
                AdministrationServerRegister.removeRobot(singleProcessName);
                System.out.println("\033[1;33m" +
                        "Successfully removed peer -" + singleProcessName + "- from the" +
                        " Administration server!" + "\033[0m");
            }
        }
    }

    private static void waitNextIteration(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLastRobotID() {
        return AdministrationServerRegister.getCleaningRobots().get(0).getId();
    }

    private long getLastMessageTimestamp() {
        return  AdministrationServerRegister.getAirPollutionStats()
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(v -> v.getRobotID().equals(singleProcessName))
                .map(AirPollutionMeasurement::getTimestamp)
                .max(Comparator.naturalOrder())
                .orElse((long)0);
    }
}
