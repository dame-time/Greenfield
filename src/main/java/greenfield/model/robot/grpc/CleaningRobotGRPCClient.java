package greenfield.model.robot.grpc;

import greenfield.model.robot.CleaningRobot;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.RobotServiceGrpc;
import proto.RobotServiceOuterClass;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CleaningRobotGRPCClient {
    private final CleaningRobot referenceRobot;
    private final PeerRegistry peerRegistry;

    public CleaningRobotGRPCClient(CleaningRobot referenceRobot, PeerRegistry peerRegistry) {
        this.referenceRobot = referenceRobot;
        this.peerRegistry = peerRegistry;
    }

    public void sendRobotData(CleaningRobot robot) {
        String id = robot.getId();

        PeerRegistry.Peer peer = peerRegistry.getPeer(id);
        if (peer == null) {
            throw new IllegalArgumentException("No robot with ID " + id);
        }

        RobotServiceOuterClass.Server robotServer = RobotServiceOuterClass.Server
                .newBuilder()
                .setHost("localhost")
                .setPort(robot.getgRPCListenerChannel())
                .build();

        RobotServiceOuterClass.Position robotPosition = RobotServiceOuterClass.Position
                .newBuilder()
                .setX(robot.getPosition().getX())
                .setY(robot.getPosition().getY())
                .build();

        RobotServiceOuterClass.RobotNetworkRequest request = RobotServiceOuterClass.RobotNetworkRequest
                .newBuilder()
                .setId(id)
                .setPosition(robotPosition)
                .setRobotServer(robotServer)
                .build();

        StreamObserver<RobotServiceOuterClass.RobotNetworkResponse> responseObserver =
                new StreamObserver<>() {
                    @Override
                    public void onNext(RobotServiceOuterClass.RobotNetworkResponse response) {
                        System.out.println("Server response received");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("Error occurred: " + throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Peer update completed.");
                    }
                };

        peer.stub.sendConnectionInfo(request, responseObserver);
    }

    public void broadcastRobotData() {
        RobotServiceOuterClass.Server robotServer = RobotServiceOuterClass.Server
                .newBuilder()
                .setHost("localhost")
                .setPort(this.referenceRobot.getgRPCListenerChannel())
                .build();

        RobotServiceOuterClass.Position robotPosition = RobotServiceOuterClass.Position
                .newBuilder()
                .setX(this.referenceRobot.getPosition().getX())
                .setY(this.referenceRobot.getPosition().getY())
                .build();

        RobotServiceOuterClass.RobotNetworkRequest request = RobotServiceOuterClass.RobotNetworkRequest
                .newBuilder()
                .setId(this.referenceRobot.getId())
                .setRobotServer(robotServer)
                .setPosition(robotPosition)
                .build();

        StreamObserver<RobotServiceOuterClass.RobotNetworkResponse> responseObserver =
                new StreamObserver<>() {
                    @Override
                    public void onNext(RobotServiceOuterClass.RobotNetworkResponse response) {
                        System.out.println("Server response received");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("Error occurred: " + throwable.getMessage());

                        if (throwable instanceof io.grpc.StatusRuntimeException statusException) {
                            // Check if the status is UNAVAILABLE, which typically means the server is not running
                            if (statusException.getStatus().getCode() == io.grpc.Status.UNAVAILABLE.getCode()) {
                                System.out.println("Server for robot " + referenceRobot.getId() +
                                        " is unavailable, removing from map");

                                peerRegistry.removeRobot(referenceRobot.getId());
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Peer update completed.");
                    }
                };

        for (Map.Entry<String, PeerRegistry.Peer> entry : peerRegistry.getConnectedPeers().entrySet()) {
            if (!entry.getKey().equals(referenceRobot.getId())) {
                entry.getValue().stub.sendConnectionInfo(request, responseObserver);
            }
        }
    }

    public void sayGoodbyeToAll() {
        RobotServiceOuterClass.GoodbyeRequest request = RobotServiceOuterClass.GoodbyeRequest.newBuilder()
                .setId(this.referenceRobot.getId())
                .build();

        StreamObserver<RobotServiceOuterClass.GoodbyeResponse> responseObserver =
                new StreamObserver<>() {
                    @Override
                    public void onNext(RobotServiceOuterClass.GoodbyeResponse response) {
                        if (response.getResult()) {
                            System.out.println("Successfully said goodbye. Removing robot from local map.");
                        } else {
                            System.err.println("Failed to say goodbye.");
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.err.println("Error occurred during goodbye: " + throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Goodbye process completed.");
                    }
                };

        for (Map.Entry<String, PeerRegistry.Peer> entry : peerRegistry.getConnectedPeers().entrySet()) {
            if (!entry.getKey().equals(this.referenceRobot.getId())) {
                entry.getValue().stub.sayGoodbye(request, responseObserver);
            }
        }
    }
}