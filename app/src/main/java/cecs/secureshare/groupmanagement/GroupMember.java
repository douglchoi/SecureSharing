package cecs.secureshare.groupmanagement;

import org.spongycastle.openpgp.PGPPublicKey;

import java.net.Socket;

import cecs.secureshare.connector.host.ClientConnectionThread;

/**
 * Created by Douglas on 12/3/2015.
 */
public class GroupMember {

    private String name;
    private PGPPublicKey publicKey;
    private ClientConnectionThread clientConn;

    public GroupMember(ClientConnectionThread clientConn) {
        this.clientConn = clientConn;
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
