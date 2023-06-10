package greenfield.model.robot.networking;

import greenfield.model.adminServer.AdministrationServer;
import greenfield.model.robot.grpc.CleaningRobotGRPCClient;
import greenfield.model.robot.grpc.PeerRegistry;
import greenfield.model.robot.utils.BalancingACKReceiver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

public class HeartbeatChecker extends Thread {

    private final CleaningRobotGRPCClient client;
    private final PeerRegistry registry;
    private boolean isRunning = true;

    public HeartbeatChecker(CleaningRobotGRPCClient client, PeerRegistry registry) {
        this.client = client;
        this.registry = registry;
        this.isRunning = true;
    }

    public synchronized void shutDownHeartbeatChecker() {
        this.isRunning = false;
        this.notifyAll();
    }

    @Override
    public void run() {
        while (isRunning) {
            client.broadcastHeartbeat();
            checkForCrashes();

            synchronized (this) {
                try {
                    wait(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void checkForCrashes() {
        Instant now = Instant.now();

        for (PeerRegistry.Peer peer : registry.getConnectedPeers().values()) {
            // 9 seconds of interval before I remove a peer from my map, basically I give him 3 window to answer properly
            int THRESHOLD = 9;
            if (now.getEpochSecond() - peer.lastHeartbeat.getEpochSecond() > THRESHOLD) {
                System.out.println("CRASH NOTICED!");
                registry.removeRobot(peer.id);
                registry.getRobotMechanic().awakeDueToPeerFailure();

                removeFailingRobotFromAdministrationServer(peer.id);
            }
        }
    }

    private void removeFailingRobotFromAdministrationServer(String robotID) {
        String serverURL = AdministrationServer.BASE_URI;
        String deleteRequestURL = "robots/delete/" + robotID;

        try {
            URL url = new URL(serverURL + deleteRequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("\033[1;33m" +
                        "Successfully removed peer -" + robotID + "- from the" +
                        " Administration server!" + "\033[0m");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
