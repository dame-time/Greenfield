package greenfield.model.adminServer;

import utils.data.AirPollutionMeasurement;

import java.util.Comparator;
import java.util.List;

public class SingleCrashedRobotHandler extends Thread {
    private String singleProcessName;
    private long lastMessageTimestamp;
    private boolean isRunning;

    public SingleCrashedRobotHandler() {
        lastMessageTimestamp = 0;
        singleProcessName = "";
        this.isRunning = true;
    }

    public synchronized void shutDownSingleProcessCrashHandler() {
        this.isRunning = false;
        this.interrupt();
    }

    public void awake() {
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (AdministrationServerRegister.getCleaningRobots().size() != 1) {
                singleProcessName = "";
                lastMessageTimestamp = 0;
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                continue;
            }

            System.out.println("CHECKING IF A SINGLE ROBOT IS CRASHED....");

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

    private void waitNextIteration(long waitTime) {
        synchronized (this) {
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
