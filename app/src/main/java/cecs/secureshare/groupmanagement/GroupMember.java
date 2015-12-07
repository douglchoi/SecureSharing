package cecs.secureshare.groupmanagement;

import org.spongycastle.openpgp.PGPPublicKey;

import cecs.secureshare.connector.host.ClientConnectionThread;
import cecs.secureshare.security.CryptoManager;

/**
 * Created by Douglas on 12/3/2015.
 */
public class GroupMember {

    private String name;
    private PGPPublicKey publicKey;
    private ClientConnectionThread clientConn;

    /**
     * @param clientConn
     * @param encodedPublicKeyRing
     */
    public GroupMember(ClientConnectionThread clientConn, byte[] encodedPublicKeyRing) {
        this.clientConn = clientConn;
        publicKey = CryptoManager.extractPublicKey(encodedPublicKeyRing);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientConnectionThread getClientConn() {
        return clientConn;
    }

    public void setClientConn(ClientConnectionThread clientConn) {
        this.clientConn = clientConn;
    }

    public PGPPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PGPPublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
