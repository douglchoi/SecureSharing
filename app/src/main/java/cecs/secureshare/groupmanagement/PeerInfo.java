package cecs.secureshare.groupmanagement;

import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;

import cecs.secureshare.connector.client.JoinGroupService;
import cecs.secureshare.security.CryptoManager;

/**
 * This object contains the information abeout the client and the reference to the service
 * object holding the socket connection to th host.
 * Created by Harshal on 12/6/2015.
 */
public class PeerInfo {

    private static PeerInfo ourInstance = new PeerInfo();

    public static PeerInfo getInstance() {
        return ourInstance;
    }

    private PGPPublicKey hostPublicKey;
    private JoinGroupService currentConn;

    private PeerInfo() { }

    public JoinGroupService getCurrentConn() {
        return currentConn;
    }

    public void setCurrentConn(JoinGroupService currentConn) {
        this.currentConn = currentConn;
    }

    public PGPPublicKey getHostPublicKey() {
        return hostPublicKey;
    }

    public void setHostPublicKey(PGPPublicKey hostPublicKey) {
        this.hostPublicKey = hostPublicKey;
    }

    public void setHostPublicKey(byte[] encodedHostPublicKeyRing) {
        this.hostPublicKey = CryptoManager.extractPublicKey(encodedHostPublicKeyRing);
    }
}
