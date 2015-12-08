package cecs.secureshare.groupmanagement;

import org.spongycastle.openpgp.PGPPublicKey;

import cecs.secureshare.connector.client.JoinGroupService;
import cecs.secureshare.security.CryptoManager;
import cecs.secureshare.security.PGPEncryptSigningPublicKey;

/**
 * This object contains the information about the client and the reference to the service
 * object holding the socket connection to th host.
 * Created by Harshal on 12/6/2015.
 */
public class PeerInfo {

    private static PeerInfo ourInstance = new PeerInfo();

    public static PeerInfo getInstance() {
        return ourInstance;
    }

    private PGPPublicKey hostPublicKey;
    private PGPPublicKey hostSigningPublicKey;
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

    /**
     * Sets the host public keys
     * @param encodedHostPublicKeyRing
     */
    public void setHostPublicKeys(byte[] encodedHostPublicKeyRing) {
        PGPEncryptSigningPublicKey keys = CryptoManager.extractPublicKey(encodedHostPublicKeyRing);
        hostPublicKey = keys.getEncryptKey();
        hostSigningPublicKey = keys.getSigningKey();
    }

    public PGPPublicKey getHostSigningPublicKey() {
        return hostSigningPublicKey;
    }
}
