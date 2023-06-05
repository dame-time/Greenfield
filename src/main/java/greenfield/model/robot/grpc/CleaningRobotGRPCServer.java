package greenfield.model.robot.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import proto.RobotServiceGrpc;
import proto.RobotServiceOuterClass;
import utils.data.Position;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CleaningRobotGRPCServer {
    private Server server;
    private final int port;
    private final PeerRegistry peerRegistry;
    private final List<MutexRequestHandler> queuedMutexRequests;

    public CleaningRobotGRPCServer(int port, PeerRegistry peerRegistry) {
        this.port = port;
        this.peerRegistry = peerRegistry;
        this.queuedMutexRequests = new ArrayList<>();
    }

    public void start() throws IOException {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(new RobotServiceImpl(port, "localhost", peerRegistry,
                            queuedMutexRequests))
                    .build()
                    .start();
        } catch (IOException e) {
            System.err.println("Failed to start the gRPC server: " + e.getMessage());
            throw e;
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void releaseMutex() {
        for (var bufferedRequests : this.queuedMutexRequests)
            bufferedRequests.start();

        this.queuedMutexRequests.clear();
    }

    static class RobotServiceImpl extends RobotServiceGrpc.RobotServiceImplBase {
        private final String serverHost;
        private final int serverPort;
        private final PeerRegistry peerRegistry;
        private final List<MutexRequestHandler> queuedMutexRequests;

        public RobotServiceImpl(int serverPort, String serverHost, PeerRegistry peerRegistry,
                                List<MutexRequestHandler> queuedMutexRequests) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
            this.peerRegistry = peerRegistry;
            this.queuedMutexRequests = queuedMutexRequests;
        }

        @Override
        public void sendPeerBalancingRequest(RobotServiceOuterClass.PeerBalancingRequest request,
                                             StreamObserver<RobotServiceOuterClass.PeerBalancingResponse> responseObserver) {
            var cellToBalance = peerRegistry.getDistrict().findCellsToBalance();
            var balancedPosition = new Position<Integer, Integer>(
                    request.getPeerBalancingInfo().getBalancedPosition().getX(),
                    request.getPeerBalancingInfo().getBalancedPosition().getY()
            );
            var unBalancedPosition = new Position<Integer, Integer>(
                    request.getPeerBalancingInfo().getUnbalancedPosition().getX(),
                    request.getPeerBalancingInfo().getUnbalancedPosition().getY()
            );

            // I do that cos sometimes happens that we do not send ACKs cos we did not already received the status change
            // So I answer no, but when I retry we will know that the state of a peer/s is inconsistent and so should be
            // brought to our current state.
            boolean stateCheck = peerRegistry.getDistrict().checkIfCellIsBusy(balancedPosition) &&
                    !peerRegistry.getDistrict().checkIfCellIsBusy(unBalancedPosition);

            if (cellToBalance == null && !stateCheck) {
                RobotServiceOuterClass.PeerBalancingResponse response = RobotServiceOuterClass.PeerBalancingResponse
                        .newBuilder()
                        .setId(peerRegistry.getReferenceRobot().getId())
                        .setAgree(false)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();

                return;
            }

            RobotServiceOuterClass.PeerBalancingResponse response = RobotServiceOuterClass.PeerBalancingResponse
                    .newBuilder()
                    .setId(peerRegistry.getReferenceRobot().getId())
                    .setAgree(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void sendConnectionInfo(RobotServiceOuterClass.RobotNetworkRequest request,
                                       StreamObserver<RobotServiceOuterClass.RobotNetworkResponse> responseObserver) {
            RobotServiceOuterClass.Server serverInfo = RobotServiceOuterClass.Server
                    .newBuilder()
                    .setPort(this.serverPort)
                    .setHost(this.serverHost)
                    .build();

            String result = "Server with port - " + this.serverPort + " - received position " +
                    "from robot ~ " + request.getId() + " ~ running on " +
                    "server port + " + request.getRobotServer().getPort() + " +";

            Position<Integer, Integer> peerPosition = new Position<>(
                    request.getPosition().getX(),
                    request.getPosition().getY()
            );

            peerRegistry.addRobot(
                    request.getId(),
                    request.getRobotServer().getHost(),
                    request.getRobotServer().getPort(),
                    peerPosition,
                    request.getPosition().getDistrictNumber()
            );

            RobotServiceOuterClass.RobotNetworkResponse response = RobotServiceOuterClass.RobotNetworkResponse
                    .newBuilder()
                    .setId(request.getId())
                    .setReceiverServer(serverInfo)
                    .setResult(result)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void heartbeat(RobotServiceOuterClass.HeartbeatRequest request,
                              StreamObserver<RobotServiceOuterClass.HeartbeatResponse> responseObserver) {
            PeerRegistry.Peer peer = peerRegistry.getPeer(request.getId());

            boolean success = false;

            if (peer != null) {
                peer.updateHeartbeat();
                success = true;
            }

            RobotServiceOuterClass.HeartbeatResponse response = RobotServiceOuterClass.HeartbeatResponse
                    .newBuilder()
                    .setSuccess(success)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void requestMutex(RobotServiceOuterClass.MutexRequest request,
                                 StreamObserver<RobotServiceOuterClass.MutexResponse> responseObserver) {
            // Append to a queue of buffered nodes to answer with an OK when I am done
            if (this.peerRegistry.getRobotMechanic().isRepairing ||
                    (this.peerRegistry.getRobotMechanic().needsRepairing
                    && this.peerRegistry.getRobotMechanic().requestTimestamp < request.getTimestamp())) {
                this.queuedMutexRequests.add(
                        new MutexRequestHandler(peerRegistry.getReferenceRobot().getId(), responseObserver));
                return;
            }

            boolean ACK = true;

            RobotServiceOuterClass.MutexResponse response = RobotServiceOuterClass.MutexResponse
                    .newBuilder()
                    .setId(peerRegistry.getReferenceRobot().getId())
                    .setAck(ACK)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void sayGoodbye(RobotServiceOuterClass.GoodbyeRequest request,
                               StreamObserver<RobotServiceOuterClass.GoodbyeResponse> responseObserver) {

//            System.err.println(peerRegistry.getDistrict().getTotalNumberOfRobotsInDistricts());
            boolean success = peerRegistry.removeRobot(request.getId());
//            System.err.println(peerRegistry.getDistrict().getTotalNumberOfRobotsInDistricts());

            RobotServiceOuterClass.GoodbyeResponse response = RobotServiceOuterClass.GoodbyeResponse
                    .newBuilder()
                    .setResult(success)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}