package greenfield.model.robot.grpc;

import greenfield.model.robot.CleaningRobot;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import proto.RobotServiceGrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// TODO: Handle synchronization better
public class PeerRegistry {
    public static class Peer {
        public String id;
        public String host;
        public int port;
        public ManagedChannel channel;
        public RobotServiceGrpc.RobotServiceStub stub;

        public Peer(String id, String host, int port, ManagedChannel channel, RobotServiceGrpc.RobotServiceStub stub) {
            this.id = id;
            this.host = host;
            this.port = port;
            this.channel = channel;
            this.stub = stub;
        }
    }

    private CleaningRobot referenceRobot;
    private final Map<String, Peer> connectedPeers;

    public PeerRegistry(CleaningRobot referenceRobot) {
        connectedPeers = new HashMap<>();
        this.referenceRobot = referenceRobot;
    }

    public synchronized void addPeer(String id, String host, int port, ManagedChannel channel, RobotServiceGrpc.RobotServiceStub stub) {
        Peer newPeer = new Peer(id, host, port, channel, stub);
        connectedPeers.put(id, newPeer);

        System.out.println("The robot - " + this.referenceRobot.getId() + " - now knows how to " +
                "communicate with robot ~ " + id + " ~ at port + " + port + " +");
    }

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

        System.out.println("The robot - " + id + " - now is now removed " +
                "from peer + " + this.referenceRobot.getId() + " + list: " + this.connectedPeers);
    }

    public synchronized Peer getPeer(String id) {
        return connectedPeers.get(id);
    }

    public synchronized Map<String, Peer> getConnectedPeers() {
        return connectedPeers;
    }
}
