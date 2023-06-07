package greenfield.model.mechanic;

import greenfield.model.robot.sensors.HealthChecker;

public class Mechanic extends Thread {
    private final HealthChecker healthChecker;

    public Mechanic (HealthChecker healthChecker) {
        this.healthChecker = healthChecker;
    }

    @Override
    public void run() {
        System.out.println("Repairing robot: " + this.healthChecker.getReferenceRobot().getId());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (!this.healthChecker.shouldCrash()) {
            System.out.println("Finished repairing robot: " + this.healthChecker.getReferenceRobot().getId());

            this.healthChecker.getServer().releaseMutex();
            this.healthChecker.getPeerRegistry().leaveMechanic();
        }
    }
}
