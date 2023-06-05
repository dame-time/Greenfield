package greenfield.model.robot.utils;

import greenfield.model.robot.grpc.PeerRegistry;
import greenfield.model.robot.sensors.DistrictBalanceWatcher;

import java.util.ArrayList;
import java.util.List;

public class BalancingACKReceiver extends Thread {
    private final PeerRegistry peerRegistry;
    private final DistrictBalanceWatcher balanceWatcher;

    private final int timeout;

    public BalancingACKReceiver(PeerRegistry peerRegistry, DistrictBalanceWatcher districtBalanceWatcher) {
        this.peerRegistry = peerRegistry;
        this.balanceWatcher = districtBalanceWatcher;

        this.timeout = 30;

        this.peerRegistry.initializeChangeDistrictsACK();
    }

    public synchronized void shutDownBalancingACKReceiver() {
        this.interrupt();
    }

    @Override
    public void run() {
        System.err.println("RUNNING BALANCER...");
        int cycles = 0;
        while (!areAllACKsReceived() && !Thread.currentThread().isInterrupted()) {
            if (cycles >= timeout) {
                System.err.println("TIMEOUT IN ACKs FOR REBALANCE FROM PEER -" +
                        this.peerRegistry.getReferenceRobot().getId() + "- " +
                        " \n with position: " + this.peerRegistry.getReferenceRobot().getPosition() + " \n " +
                        " with ACKs: " + peerRegistry.getChangeDistrictsACKReceived()
                        .values()
                        .stream()
                        .filter(e -> e)
                        .toList()
                        .size()
                );
                this.peerRegistry.clearChangeDistrictsACK();
                this.balanceWatcher.setBalancerRunning(false);
                return; // leaving the method if I reached a timeout
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            clearDeadPeersACKs();
            ++cycles;
        }

        this.peerRegistry.balanceDistrictCells();
        this.peerRegistry.clearChangeDistrictsACK();
        this.balanceWatcher.setBalancerRunning(false);
    }

    private boolean areAllACKsReceived() {
        return this.peerRegistry
                .getChangeDistrictsACKReceived()
                .values()
                .stream()
                .filter(e -> e)
                .toList()
                .size() >=
                (this.peerRegistry
                        .getConnectedPeers()
                        .values()
                        .size() / 2);
    }

    private void clearDeadPeersACKs() {
        List<String> deadPeers = new ArrayList<>();
        for (String peerID : peerRegistry.getChangeDistrictsACKReceived().keySet())
            if (peerRegistry.getChangeDistrictsACKReceived().get(peerID) == null)
                deadPeers.add(peerID);

        for (String peer : deadPeers)
            peerRegistry.removeChangeDistrictsACK(peer);
    }
}
