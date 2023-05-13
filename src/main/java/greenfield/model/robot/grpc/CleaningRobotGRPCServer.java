package greenfield.model.robot.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import proto.RobotServiceGrpc;
import proto.RobotServiceOuterClass;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CleaningRobotGRPCServer {
    private Server server;
    private final int port;
    private final PeerRegistry peerRegistry;

    public CleaningRobotGRPCServer(int port, PeerRegistry peerRegistry) {
        this.port = port;
        this.peerRegistry = peerRegistry;
    }

    public void start() throws IOException {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(new RobotServiceImpl(port, "localhost", peerRegistry))
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

    static class RobotServiceImpl extends RobotServiceGrpc.RobotServiceImplBase {
        private final String serverHost;
        private final int serverPort;
        private final PeerRegistry peerRegistry;

        public RobotServiceImpl(int serverPort, String serverHost, PeerRegistry peerRegistry) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
            this.peerRegistry = peerRegistry;
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