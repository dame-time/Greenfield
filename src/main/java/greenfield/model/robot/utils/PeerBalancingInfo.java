package greenfield.model.robot.utils;

public class PeerBalancingInfo {
    private String peerID;
    private int newDistrict;

    public PeerBalancingInfo (String peerID, int newDistrict) {
        this.peerID = peerID;
        this.newDistrict = newDistrict;
    }

    public String getPeerID() {
        return peerID;
    }

    public int getNewDistrict() {
        return newDistrict;
    }
}

