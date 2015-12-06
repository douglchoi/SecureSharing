package cecs.secureshare.groupmanagement;

import java.net.Socket;

import cecs.secureshare.connector.client.JoinGroupService;

/**
 * Created by Harshal on 12/6/2015.
 */
public class PeerInfo {
    private static PeerInfo ourInstance = new PeerInfo();

    public static PeerInfo getInstance() {
        return ourInstance;
    }

    private JoinGroupService currentConn;

    private PeerInfo() {
    }

    public JoinGroupService getCurrentConn() {
        return currentConn;
    }

    public void setCurrentConn(JoinGroupService currentConn) {
        this.currentConn = currentConn;
    }
}
