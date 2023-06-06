package greenfield.model.robot.grpc;

import greenfield.model.robot.CleaningRobot;
import io.grpc.stub.StreamObserver;
import proto.RobotServiceOuterClass;

public class MutexRequestHandler extends Thread {
    private final String responseID;
    private final StreamObserver<RobotServiceOuterClass.MutexResponse> responseObserver;
    private PeerRegistry peerRegistry;

    public MutexRequestHandler(String responseID,
                               StreamObserver<RobotServiceOuterClass.MutexResponse> responseObserver,
                               PeerRegistry peerRegistry) {
        this.responseID = responseID;
        this.responseObserver = responseObserver;
        this.peerRegistry = peerRegistry;
    }

    @Override
    public void run() {
        boolean ACK = true;

        RobotServiceOuterClass.MutexResponse response = RobotServiceOuterClass.MutexResponse
                .newBuilder()
                .setId(responseID)
                .setAck(ACK)
                .setLogicalTimestamp(this.peerRegistry.getReferenceRobot().getLogicalClock().getClock())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
