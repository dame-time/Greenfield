package greenfield.model.robot.grpc;

import greenfield.model.adminServer.AdministrationServer;
import greenfield.model.robot.CleaningRobot;
import greenfield.model.robot.utils.BalancingACKReceiver;
import greenfield.model.robot.utils.PeerBalancingInfo;
import io.grpc.stub.StreamObserver;
import proto.RobotServiceOuterClass;
import utils.data.DistrictCell;
import utils.data.Pair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class CleaningRobotGRPCClient {
    private final CleaningRobot referenceRobot;
    private final PeerRegistry peerRegistry;

    public CleaningRobotGRPCClient(CleaningRobot referenceRobot, PeerRegistry peerRegistry) {
        this.referenceRobot = referenceRobot;
        this.peerRegistry = peerRegistry;
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
                .setDistrictNumber(this.referenceRobot.getDistrictCellNumber())
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

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("Error occurred: " + throwable.getMessage());

                        if (throwable instanceof io.grpc.StatusRuntimeException statusException) {
                            if (statusException.getStatus().getCode() == io.grpc.Status.UNAVAILABLE.getCode()) {
                                System.out.println("Server for robot " + referenceRobot.getId() +
                                        " is unavailable, removing from map");

                                peerRegistry.removeRobot(referenceRobot.getId());

                                removeFailingRobotFromAdministrationServer(referenceRobot.getId());
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                };

        for (Map.Entry<String, PeerRegistry.Peer> entry : peerRegistry.getConnectedPeers().entrySet()) {
            if (!entry.getKey().equals(referenceRobot.getId())) {
                entry.getValue().stub.sendConnectionInfo(request, responseObserver);
            }
        }
    }

    public void broadcastMutualExclusion() {
        RobotServiceOuterClass.MutexRequest request = RobotServiceOuterClass.MutexRequest
                .newBuilder()
                .setId(this.referenceRobot.getId())
                .setTimestamp(this.referenceRobot.getInternalClock().getTime())
                .setLogicalTimestamp(this.referenceRobot.getLogicalClock().getClock())
                .build();

        StreamObserver<RobotServiceOuterClass.MutexResponse> responseObserver =
                new StreamObserver<>() {
                    @Override
                    public void onNext(RobotServiceOuterClass.MutexResponse response) {
                        if (response.getAck()) {
                            System.out.println("Peer -" + referenceRobot.getId() + "- received ACK from +" + response.getId()+ "+");
                            peerRegistry.getRobotMechanic().mutexACKReceived.put(response.getId(), response.getAck());
                        }

                        referenceRobot.getLogicalClock().compareAndUpdate(response.getLogicalTimestamp());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.err.println("Error occurred while requesting mutual exclusion: " + throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Peer -" + referenceRobot.getId() + "- received: "
                                + peerRegistry.getRobotMechanic().mutexACKReceived + "ACKs");
                    }
                };

        for (Map.Entry<String, PeerRegistry.Peer> entry : peerRegistry.getConnectedPeers().entrySet()) {
            if (!entry.getKey().equals(this.referenceRobot.getId())) {
                peerRegistry.getRobotMechanic().mutexACKReceived.put(entry.getKey(), false);
                entry.getValue().stub.requestMutex(request, responseObserver);
            }
        }
    }

    public void broadcastHeartbeat() {
        RobotServiceOuterClass.HeartbeatRequest request = RobotServiceOuterClass.HeartbeatRequest
                .newBuilder()
                .setId(this.referenceRobot.getId())
                .build();

        for (Map.Entry<String, PeerRegistry.Peer> entry : peerRegistry.getConnectedPeers().entrySet()) {
            if (!entry.getKey().equals(referenceRobot.getId())) {
                StreamObserver<RobotServiceOuterClass.HeartbeatResponse> responseObserver =
                        new StreamObserver<>() {
                            @Override
                            public void onNext(RobotServiceOuterClass.HeartbeatResponse response) {

                            }

                            @Override
                            public void onError(Throwable throwable) {
                                System.err.println("Error occurred while sending heartbeat: " + throwable.getMessage());
                                if (throwable instanceof io.grpc.StatusRuntimeException statusException) {
                                    if (statusException.getStatus().getCode() == io.grpc.Status.UNAVAILABLE.getCode()) {
                                        System.out.println("Peer " + entry.getKey() + " is unavailable, removing from map");
                                        peerRegistry.removeRobot(entry.getKey());

                                        removeFailingRobotFromAdministrationServer(entry.getKey());
                                    }
                                }
                            }

                            @Override
                            public void onCompleted() {

                            }
                        };

                entry.getValue().stub.heartbeat(request, responseObserver);
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

                            removeFailingRobotFromAdministrationServer(request.getId());

                            peerRegistry.getDistrict().removeRobotFromPosition(referenceRobot.getPosition());
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

                    }
                };

        for (Map.Entry<String, PeerRegistry.Peer> entry : peerRegistry.getConnectedPeers().entrySet())
            if (!entry.getKey().equals(this.referenceRobot.getId()))
                entry.getValue().stub.sayGoodbye(request, responseObserver);
    }

    private static void removeFailingRobotFromAdministrationServer(String robotID) {
        String serverURL = AdministrationServer.BASE_URI;
        String deleteRequestURL = "robots/delete/" + robotID;

        try {
            URL url = new URL(serverURL + deleteRequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("\033[1;33m" +
                        "Successfully removed peer -" + robotID + "- from the" +
                        " Administration server!" + "\033[0m");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastPeerBalancingInfo(Pair<DistrictCell, DistrictCell> districtCells) {
        RobotServiceOuterClass.Position grpcUnbalancedPosition = RobotServiceOuterClass.Position
                .newBuilder()
                .setDistrictNumber(districtCells.getFirst().districtNumber)
                .setX(districtCells.getFirst().position.getX())
                .setX(districtCells.getFirst().position.getY())
                .build();

        RobotServiceOuterClass.Position grpcBalancedPosition = RobotServiceOuterClass.Position
                .newBuilder()
                .setDistrictNumber(districtCells.getSecond().districtNumber)
                .setX(districtCells.getSecond().position.getX())
                .setX(districtCells.getSecond().position.getY())
                .build();

        RobotServiceOuterClass.PeerBalancingInfo grpcPeerBalancingInfo =
                RobotServiceOuterClass.PeerBalancingInfo
                        .newBuilder()
                        .setUnbalancedPosition(grpcUnbalancedPosition)
                        .setBalancedPosition(grpcBalancedPosition)
                        .build();

        RobotServiceOuterClass.PeerBalancingRequest request =
                RobotServiceOuterClass.PeerBalancingRequest
                        .newBuilder()
                        .setSenderID(this.referenceRobot.getId())
                        .setPeerBalancingInfo(grpcPeerBalancingInfo)
                        .build();

        StreamObserver<RobotServiceOuterClass.PeerBalancingResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(RobotServiceOuterClass.PeerBalancingResponse response) {
                if (response.getAgree()) {
                    peerRegistry.addChangeDistrictsACK(response.getId());
                    System.out.println("Peer -" + referenceRobot.getId() + "- received district " +
                            "ACK from +" + response.getId() + "+ total: " + peerRegistry.getChangeDistrictsACKReceived());
                }
                else {
                    System.err.println("Peer -" + referenceRobot.getId() + "- did not receive district " +
                            "ACK from +" + response.getId() + "+");
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };

        for (Map.Entry<String, PeerRegistry.Peer> entry : peerRegistry.getConnectedPeers().entrySet())
            if (!entry.getKey().equals(this.referenceRobot.getId()))
                entry.getValue().stub.sendPeerBalancingRequest(request, responseObserver);
    }
}