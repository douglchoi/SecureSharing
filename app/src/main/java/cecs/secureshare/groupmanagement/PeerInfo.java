package cecs.secureshare.groupmanagement;

import cecs.secureshare.connector.client.JoinGroupService;

/**
 * This object contains the information about the client and the reference to the service
 * object holding the socket connection to the host.
 * Created by Harshal on 12/6/2015.
 */
public class PeerInfo {


    private static PeerInfo ourInstance = new PeerInfo();

    public static PeerInfo getInstance() {
        return ourInstance;
    }

    private JoinGroupService currentConn;

    private PeerInfo() { }

    public JoinGroupService getCurrentConn() {
        return currentConn;
    }

    public void setCurrentConn(JoinGroupService currentConn) {
        this.currentConn = currentConn;
    }
}
