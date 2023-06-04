package greenfield.model.robot.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import proto.RobotServiceGrpc;
import proto.RobotServiceOuterClass;

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

            peerRegistry.addRobot(request.getId(), request.getRobotServer().getHost(), request.getRobotServer().getPort());

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

            peerRegistry.removeRobot(request.getId());

            boolean success = true;

            RobotServiceOuterClass.GoodbyeResponse response = RobotServiceOuterClass.GoodbyeResponse
                    .newBuilder()
                    .setResult(success)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}