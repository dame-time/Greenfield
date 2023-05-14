package greenfield.model.robot.sensors;

import greenfield.model.robot.CleaningRobot;
import greenfield.model.robot.grpc.CleaningRobotGRPCClient;
import greenfield.model.robot.grpc.CleaningRobotGRPCServer;
import greenfield.model.robot.grpc.PeerRegistry;
import proto.RobotServiceOuterClass;

import java.io.IOException;
import java.util.List;

public class HealthChecker extends Thread {
    private final CleaningRobot referenceRobot;

    private final CleaningRobotGRPCServer server;
    private final CleaningRobotGRPCClient client;
    private final PeerRegistry peerRegistry;

    private final PositionWatcher positionWatcher;

    public HealthChecker(CleaningRobot cleaningRobot, List<CleaningRobot> robotsNetwork) {
        this.referenceRobot = cleaningRobot;

        this.peerRegistry = new PeerRegistry(this.referenceRobot);
        this.server = new CleaningRobotGRPCServer(cleaningRobot.getgRPCListenerChannel(), this.peerRegistry);

        try {
            this.server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.client = new CleaningRobotGRPCClient(this.referenceRobot, this.peerRegistry);

        for (CleaningRobot robot : robotsNetwork) {
            this.peerRegistry.addRobot(robot.getId(), "localhost", robot.getgRPCListenerChannel());
        }

        this.client.broadcastRobotData();

        this.positionWatcher = new PositionWatcher(this.referenceRobot, this.client);
        this.positionWatcher.start();
    }

    public synchronized void shutDownHealthChecker() {
        this.interrupt();
    }

    @Override
    public void run() {
        // TODO: handle mechanic logic
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        while (!Thread.currentThread().isInterrupted()) {
            // TODO: handle mechanic logic
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        this.client.sayGoodbyeToAll();
        this.positionWatcher.shutDownWatcher();
    }
}
