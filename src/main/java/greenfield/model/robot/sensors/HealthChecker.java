package greenfield.model.robot.sensors;

import greenfield.model.robot.CleaningRobot;
import greenfield.model.robot.grpc.CleaningRobotGRPCClient;
import greenfield.model.robot.grpc.CleaningRobotGRPCServer;
import greenfield.model.robot.grpc.PeerRegistry;
import greenfield.model.robot.networking.HeartbeatChecker;
import greenfield.model.robot.utils.ACKReceiver;

import java.io.IOException;
import java.util.List;
import java.util.Random;

// TODO: Change/delete the position watcher thread
public class HealthChecker extends Thread {
    private final CleaningRobot referenceRobot;

    private final CleaningRobotGRPCServer server;
    private final CleaningRobotGRPCClient client;
    private final PeerRegistry peerRegistry;

    private final Random randomGenerator;
    private double chance;

    private final PositionWatcher positionWatcher;
    private final HeartbeatChecker heartbeatChecker;

    private boolean shouldCrash;

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

        for (CleaningRobot robot : robotsNetwork)
            this.peerRegistry.addRobot(robot.getId(),
                    "localhost",
                    robot.getgRPCListenerChannel(),
                    robot.getPosition(),
                    robot.getDistrictCellNumber());

        this.client.broadcastRobotData();

        this.randomGenerator = new Random();
        this.chance = this.randomGenerator.nextDouble();

        this.positionWatcher = new PositionWatcher(this.referenceRobot, this.client);
        this.heartbeatChecker = new HeartbeatChecker(this.client, this.peerRegistry);
//        this.positionWatcher.start();
        this.heartbeatChecker.start();

        this.shouldCrash = false;
    }

    public synchronized void shutDownHealthChecker() {
        this.interrupt();
    }

    public synchronized void crashHealthChecker() {
        this.shouldCrash = true;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            this.chance = this.randomGenerator.nextDouble();
            if (this.chance < 0.1 &&
                    !(this.getPeerRegistry().getRobotMechanic().needsRepairing ||
                        this.getPeerRegistry().getRobotMechanic().isRepairing
                    )
            )
                askForMechanic();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!shouldCrash)
            this.client.sayGoodbyeToAll();
//        this.positionWatcher.shutDownWatcher();
        this.heartbeatChecker.shutDownHeartbeatChecker();
    }

    public synchronized void askForMechanic() {
        // If I ask for a mechanic while I already asked for it, apart from being dumb, I avoid a replicated ask
        if (this.getPeerRegistry().getRobotMechanic().needsRepairing ||
            this.getPeerRegistry().getRobotMechanic().isRepairing)
            return;

        this.referenceRobot.getLogicalClock().update();

        this.getPeerRegistry().getRobotMechanic().needsRepairing = true;
        this.getPeerRegistry().getRobotMechanic().requestTimestamp = this.referenceRobot.getInternalClock().getTime();
        this.getPeerRegistry().getRobotMechanic().requestLogicalTimestamp = this.referenceRobot.getLogicalClock().getClock();

        this.client.broadcastMutualExclusion();
        new ACKReceiver(this).start();
    }

    public synchronized PeerRegistry getPeerRegistry() {
        return peerRegistry;
    }

    public synchronized CleaningRobot getReferenceRobot() {
        return referenceRobot;
    }

    public synchronized CleaningRobotGRPCClient getClient() {
        return client;
    }

    public synchronized CleaningRobotGRPCServer getServer() {
        return server;
    }
}
