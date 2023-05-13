package greenfield.model.robot.sensors;

import greenfield.model.robot.CleaningRobot;
import greenfield.model.robot.grpc.CleaningRobotGRPCClient;
import proto.RobotServiceOuterClass;
import utils.data.Position;

import java.util.Objects;

public class PositionWatcher extends Thread {
    private final CleaningRobot referenceRobot;
    private final CleaningRobotGRPCClient client;

    private boolean shutDown;
    private Position<Integer, Integer> startRobotPosition;

    public PositionWatcher(CleaningRobot referenceRobot, CleaningRobotGRPCClient client) {
        this.referenceRobot = referenceRobot;
        this.client = client;

        this.startRobotPosition = referenceRobot.getPosition();

        this.shutDown = false;
    }

    public synchronized void shutDownWatcher() {
        this.shutDown = true;
    }

    @Override
    public void run() {
        while (!this.shutDown) {
            if (Objects.equals(this.startRobotPosition, this.referenceRobot.getPosition()))
                continue;

            this.client.broadcastRobotData();

            this.startRobotPosition = this.referenceRobot.getPosition();
        }
    }
}
