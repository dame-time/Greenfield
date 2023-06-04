package greenfield.model.robot.grpc;

import io.grpc.stub.StreamObserver;
import proto.RobotServiceOuterClass;

public class MutexRequestHandler extends Thread {
    private final String responseID;
    private final StreamObserver<RobotServiceOuterClass.MutexResponse> responseObserver;

    public MutexRequestHandler(String responseID,
                               StreamObserver<RobotServiceOuterClass.MutexResponse> responseObserver) {
        this.responseID = responseID;
        this.responseObserver = responseObserver;
    }

    @Override
    public void run() {
        boolean ACK = true;

        RobotServiceOuterClass.MutexResponse response = RobotServiceOuterClass.MutexResponse
                .newBuilder()
                .setId(responseID)
                .setAck(ACK)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
