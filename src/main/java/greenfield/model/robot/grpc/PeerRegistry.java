package greenfield.model.robot.grpc;

import greenfield.model.adminServer.District;
import greenfield.model.robot.CleaningRobot;
import greenfield.model.robot.utils.ACKReceiver;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import proto.RobotServiceGrpc;
import utils.data.DistrictCell;
import utils.data.Pair;
import utils.data.Position;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PeerRegistry {
    /**
     * Class used in order to represent an individual peer in the system.
     */
    public static class Peer {
        public String id;
        public String host;
        public int port;
        public Position<Integer, Integer> districtPosition;
        public int districtNumber;
        public ManagedChannel channel;
        public RobotServiceGrpc.RobotServiceStub stub;
        public Instant lastHeartbeat;

        public Peer(String id,
                    String host,
                    int port,
                    ManagedChannel channel,
                    RobotServiceGrpc.RobotServiceStub stub,
                    Position<Integer, Integer> districtPosition,
                    int districtNumber) {
            this.id = id;
            this.host = host;
            this.port = port;
            this.channel = channel;
            this.stub = stub;
            this.lastHeartbeat = Instant.now();
            this.districtPosition = districtPosition;
            this.districtNumber = districtNumber;
        }

        public void updateHeartbeat() {
            lastHeartbeat = Instant.now();
        }
    }

    public static class RobotMechanic {
        public boolean needsRepairing;
        public boolean isRepairing;
        public long requestTimestamp;
        public List<Peer> waitingPeers;
        public int requestLogicalTimestamp;
        public ACKReceiver ackReceiverThread;

        private final Map<String, Boolean> mutexACKReceived;

        public RobotMechanic() {
            this.needsRepairing = false;
            this.isRepairing = false;
            this.mutexACKReceived = new HashMap<>();
            this.requestTimestamp = -1;
            this.requestLogicalTimestamp = -1;
            this.waitingPeers = new ArrayList<>();
        }

        public RobotMechanic(RobotMechanic copy) {
            this.needsRepairing = copy.needsRepairing;
            this.isRepairing = copy.isRepairing;
            this.mutexACKReceived = copy.mutexACKReceived;
            this.requestTimestamp = copy.requestTimestamp;
            this.waitingPeers = copy.waitingPeers;
            this.requestLogicalTimestamp = copy.requestLogicalTimestamp;
        }

        public Map<String, Boolean> getMutexACKReceived() {
            synchronized (this) {
                return mutexACKReceived;
            }
        }

        public void putACKInReceivedACKs(String robotID, boolean ack) {
            synchronized (this) {
                mutexACKReceived.put(robotID, ack);
                this.notifyAll();
            }
        }

        public void waitForACKsReceived() throws InterruptedException {
            synchronized (this) {
                if (!mutexACKReceived.values().stream().allMatch(e -> e))
                    this.wait();
            }
        }

        public void awakeDueToPeerFailure() {
            synchronized (this) {
                this.notifyAll();
            }
        }

        public boolean areAllACKsReceived() {
            synchronized (this) {
                return mutexACKReceived.values().stream().allMatch(e -> e);
            }
        }
    }

    private final RobotMechanic robotMechanic;
    private final CleaningRobot referenceRobot;
    private final Map<String, Peer> connectedPeers;

    private final Map<String, Boolean> changeDistrictsACKReceived;

    private final District district;

    /**
     * Constructor for the PeerRegistry class.
     *
     * @param referenceRobot CleaningRobot object for which this PeerRegistry is created.
     */
    public PeerRegistry(CleaningRobot referenceRobot) {
        this.connectedPeers = new HashMap<>();
        this.referenceRobot = referenceRobot;
        this.robotMechanic = new RobotMechanic();
        this.changeDistrictsACKReceived = new HashMap<>();
        this.district = new District(
                this.referenceRobot.getDistrictSize(),
                this.referenceRobot.getDistrictSize(),
                this.referenceRobot.getDistrictSubdivisions()
        );

        this.district.placeRobotAtPosition(this.referenceRobot.getPosition());
    }

    /**
     * Adding a new peer to the registry.
     *
     * @param id      The unique identifier of the peer.
     * @param host    The host address of the peer.
     * @param port    The port number for the peer.
     * @param channel The communication channel for the peer.
     * @param stub    The service stub for the peer.
     */
    public void addPeer(
                             String id,
                             String host,
                             int port,
                             ManagedChannel channel,
                             RobotServiceGrpc.RobotServiceStub stub,
                             Position<Integer, Integer> position,
                             int districtNumber
    ) {
        Peer newPeer = new Peer(id, host, port, channel, stub, position, districtNumber);
        synchronized (connectedPeers) {
            connectedPeers.put(id, newPeer);
        }
    }

    /**
     * Removal a peer from the registry.
     *
     * @param id The unique identifier of the peer.
     * @return The peer that was removed.
     */
    public Peer removePeer(String id) {
        Peer removedPeer;

        synchronized (connectedPeers) {
            removedPeer = connectedPeers.remove(id);
        }

        return removedPeer;
    }

    public void addRobot(
            String id,
            String host,
            int port,
            Position<Integer, Integer> position,
            int districtNumber
    ) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();
        RobotServiceGrpc.RobotServiceStub stub = RobotServiceGrpc.newStub(channel);

        synchronized (district) {
            this.district.placeRobotAtPosition(position);
        }

        this.addPeer(id, host, port, channel, stub, position, districtNumber);
    }

    /**
     * Method to remove a robot as a peer from the registry.
     *
     * @param id The unique identifier of the robot.
     */
    public boolean removeRobot(String id) {
        PeerRegistry.Peer peer = this.getPeer(id);
        if (peer != null) {
            synchronized (district) {
                district.removeRobotFromPosition(peer.districtPosition);
            }

            try {
                peer.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Error shutting down the channel: " + e.getMessage());
            }

            this.removePeer(id);

            return true;
        }

        return false;
    }

    /**
     * Get a peer from the registry.
     *
     * @param id The unique identifier of the peer.
     * @return The peer with the given id.
     */
    public Peer getPeer(String id) {
        return getConnectedPeers().get(id);
    }

    /**
     * Get a copy of the currently connected peers.
     *
     * @return A copy of the map of connected peers.
     */
    public synchronized Map<String, Peer> getConnectedPeers() {
        return new HashMap<>(connectedPeers); // avoid to screw up the reading of the data struct while writing on it, so I return a copy
    }

    public synchronized RobotMechanic getRobotMechanic() {
        return robotMechanic;
    }

    public void leaveMechanic() {
        this.getRobotMechanic().mutexACKReceived.clear();
        this.getRobotMechanic().needsRepairing = false;
        this.getRobotMechanic().isRepairing = false;
        this.getRobotMechanic().requestTimestamp = -1;
        this.getRobotMechanic().requestLogicalTimestamp = -1;
    }

    public synchronized CleaningRobot getReferenceRobot() {
        return referenceRobot;
    }

    public synchronized District getDistrict() {
        return district;
    }

    public void balanceDistrictCells() {
        Pair<DistrictCell, DistrictCell> cells = this.getDistrict().findCellsToBalance();

        if (cells == null)
            return;

        synchronized (district) {
            synchronized (this.referenceRobot) {
                if (this.referenceRobot.getPosition().equals(cells.getFirst().getPosition())) {
                    this.referenceRobot.setPosition(cells.getSecond().getPosition());
                    this.referenceRobot.setDistrictCell(cells.getSecond().getDistrictNumber());
                    this.referenceRobot.setMqttListenerChannel("greenfield/pollution/district" + cells.getSecond().getDistrictNumber());
                    System.err.println("++++++++++++++++++++++++++");
                }
            }

            district.removeRobotFromPosition(cells.getFirst().getPosition());
            district.placeRobotAtPosition(cells.getSecond().getPosition());
            System.err.println("Rebalanced peers was successful: " + district.getRobotsPerDistrict());
        }
    }

    public Map<String, Boolean> getChangeDistrictsACKReceived() {
        synchronized (changeDistrictsACKReceived) {
            return new HashMap<>(changeDistrictsACKReceived);
        }
    }

    public void initializeChangeDistrictsACK() {
        for (var p : getConnectedPeers().keySet()) {
            synchronized (changeDistrictsACKReceived) {
                changeDistrictsACKReceived.put(p, false);
            }
        }
    }

    public void addChangeDistrictsACK(String robotID) {
        synchronized (changeDistrictsACKReceived) {
            changeDistrictsACKReceived.put(robotID, true);
            changeDistrictsACKReceived.notifyAll();
        }
    }

    public boolean removeChangeDistrictsACK(String robotID) {
        synchronized (changeDistrictsACKReceived) {
            if (changeDistrictsACKReceived.get(robotID))
                return changeDistrictsACKReceived.remove(robotID);
        }

        return false;
    }

    public void clearChangeDistrictsACK() {
        synchronized (changeDistrictsACKReceived) {
            changeDistrictsACKReceived.clear();
        }
    }

    public boolean areAllACKsReceived() {
        synchronized (changeDistrictsACKReceived) {
            return getChangeDistrictsACKReceived()
                    .values()
                    .stream()
                    .filter(e -> e)
                    .toList()
                    .size() >= getConnectedPeers()
                                .values()
                                .size() / 2;
        }
    }

    public void waitUntilACKIsReceived() throws InterruptedException {
        synchronized (changeDistrictsACKReceived) {
            changeDistrictsACKReceived.wait();
        }
    }
}
