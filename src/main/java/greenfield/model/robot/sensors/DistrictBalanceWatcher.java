package greenfield.model.robot.sensors;

import greenfield.model.adminServer.District;
import greenfield.model.robot.utils.BalancingACKReceiver;
import utils.data.DistrictCell;
import utils.data.Pair;

public class DistrictBalanceWatcher extends Thread {
    private final HealthChecker healthChecker;
    private boolean isBalancerRunning;

    public DistrictBalanceWatcher(HealthChecker healthChecker) {
        this.healthChecker = healthChecker;
        this.isBalancerRunning = false;
    }

    public synchronized void shutDownDistrictBalanceWatcher() {
        this.interrupt();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Pair<DistrictCell, DistrictCell> cellsToBalance = null;
            if (!isBalancerRunning)
                cellsToBalance = this.healthChecker.getPeerRegistry().getDistrict().findCellsToBalance();

            if (cellsToBalance != null &&
                !isBalancerRunning
            ) {
                System.err.println("Cells to balance:" + cellsToBalance);
                this.healthChecker.getClient().broadcastPeerBalancingInfo(cellsToBalance);
                new BalancingACKReceiver(this.healthChecker.getPeerRegistry(), this).start();
                this.isBalancerRunning = true;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setBalancerRunning(boolean balancerRunning) {
        isBalancerRunning = balancerRunning;
    }
}
