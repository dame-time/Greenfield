package greenfield.model.robot.grpc;

import greenfield.model.robot.CleaningRobot;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import proto.RobotServiceGrpc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// TODO: Handle synchronization better
public class PeerRegistry {
    /**
     * Class used in order to represent an individual peer in the system.
     */
    public static class Peer {
        public String id;
        public String host;
        public int port;
        public ManagedChannel channel;
        public RobotServiceGrpc.RobotServiceStub stub;
        public Instant lastHeartbeat;

        public Peer(String id, String host, int port, ManagedChannel channel, RobotServiceGrpc.RobotServiceStub stub) {
            this.id = id;
            this.host = host;
            this.port = port;
            this.channel = channel;
            this.stub = stub;
            this.lastHeartbeat = Instant.now();
        }

        public void updateHeartbeat() {
            lastHeartbeat = Instant.now();
        }
    }

    public static class RobotMechanic {
        public boolean needsRepairing;
        public boolean isRepairing;
        public Map<String, Boolean> mutexACKReceived;
        public long requestTimestamp;
        public List<Peer> waitingPeers;

        public RobotMechanic() {
            this.needsRepairing = false;
            this.isRepairing = false;
            this.mutexACKReceived = new HashMap<>();
            this.requestTimestamp = -1;
            this.waitingPeers = new ArrayList<>();
        }

        public RobotMechanic(RobotMechanic copy) {
            this.needsRepairing = copy.needsRepairing;
            this.isRepairing = copy.isRepairing;
            this.mutexACKReceived = copy.mutexACKReceived;
            this.requestTimestamp = copy.requestTimestamp;
            this.waitingPeers = copy.waitingPeers;
        }
    }

    private final RobotMechanic robotMechanic;
    private final CleaningRobot referenceRobot;
    private final Map<String, Peer> connectedPeers;

    /**
     * Constructor for the PeerRegistry class.
     *
     * @param referenceRobot CleaningRobot object for which this PeerRegistry is created.
     */
    public PeerRegistry(CleaningRobot referenceRobot) {
        this.connectedPeers = new HashMap<>();
        this.referenceRobot = referenceRobot;
        this.robotMechanic = new RobotMechanic();
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
    public synchronized void addPeer(String id, String host, int port, ManagedChannel channel, RobotServiceGrpc.RobotServiceStub stub) {
        Peer newPeer = new Peer(id, host, port, channel, stub);
        connectedPeers.put(id, newPeer);
    }

    /**
     * Removal a peer from the registry.
     *
     * @param id The unique identifier of the peer.
     * @return The peer that was removed.
     */
    public synchronized Peer removePeer(String id) {
        return connectedPeers.remove(id);
    }

    public void addRobot(String id, String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();
        RobotServiceGrpc.RobotServiceStub stub = RobotServiceGrpc.newStub(channel);

        this.addPeer(id, host, port, channel, stub);
    }

    /**
     * Method to remove a robot as a peer from the registry.
     *
     * @param id The unique identifier of the robot.
     */
    public void removeRobot(String id) {
        PeerRegistry.Peer peer = this.getPeer(id);
        if (peer != null) {
            try {
                peer.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Error shutting down the channel: " + e.getMessage());
            }
            this.removePeer(id);
        }
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
    }

    public synchronized CleaningRobot getReferenceRobot() {
        return referenceRobot;
    }
}
