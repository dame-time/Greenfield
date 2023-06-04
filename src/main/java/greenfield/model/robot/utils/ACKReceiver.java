package greenfield.model.robot.utils;

import greenfield.model.mechanic.Mechanic;
import greenfield.model.robot.sensors.HealthChecker;

import java.util.ArrayList;
import java.util.List;

public class ACKReceiver extends Thread {
    private final HealthChecker healthChecker;

    public ACKReceiver(HealthChecker healthChecker) {
        this.healthChecker = healthChecker;
    }

    public synchronized void shutDownACKReceiver() {
        this.interrupt();
    }

    @Override
    public void run() {
        while (!areAllACKsReceived()) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            clearDeadPeersACKs();
        }

        healthChecker.getPeerRegistry().getRobotMechanic().isRepairing = true;
        new Mechanic(healthChecker).start();
    }

    private boolean areAllACKsReceived() {
        return healthChecker.getPeerRegistry().getRobotMechanic().mutexACKReceived.values().stream().allMatch(e -> e);
    }

    private void clearDeadPeersACKs() {
        List<String> deadPeers = new ArrayList<>();
        for (String peerID : healthChecker.getPeerRegistry().getRobotMechanic().mutexACKReceived.keySet())
            if (healthChecker.getPeerRegistry().getConnectedPeers().get(peerID) == null)
                deadPeers.add(peerID);

        for (String peer : deadPeers)
            healthChecker.getPeerRegistry().getRobotMechanic().mutexACKReceived.remove(peer);
    }
}
