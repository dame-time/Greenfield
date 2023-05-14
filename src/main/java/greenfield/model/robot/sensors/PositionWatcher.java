package greenfield.model.robot.sensors;

import greenfield.model.robot.CleaningRobot;
import greenfield.model.robot.grpc.CleaningRobotGRPCClient;
import proto.RobotServiceOuterClass;
import utils.data.Position;

import java.util.Objects;

public class PositionWatcher extends Thread {
    private final CleaningRobot referenceRobot;
    private final CleaningRobotGRPCClient client;

    private Position<Integer, Integer> startRobotPosition;

    public PositionWatcher(CleaningRobot referenceRobot, CleaningRobotGRPCClient client) {
        this.referenceRobot = referenceRobot;
        this.client = client;

        this.startRobotPosition = referenceRobot.getPosition();
    }

    public synchronized void shutDownWatcher() {
        this.interrupt();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (Objects.equals(this.startRobotPosition, this.referenceRobot.getPosition()))
                continue;

            this.client.broadcastRobotData();

            this.startRobotPosition = this.referenceRobot.getPosition();
        }
    }
}
