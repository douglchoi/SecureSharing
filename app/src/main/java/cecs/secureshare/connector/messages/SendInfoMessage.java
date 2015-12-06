package cecs.secureshare.connector.messages;

import org.spongycastle.openpgp.PGPPublicKey;

/**
 * Created by Douglas on 12/6/2015.
 */
public class SendInfoMessage extends Message {

    private String name;
    private PGPPublicKey publicKey;

    public SendInfoMessage(String name, PGPPublicKey publicKey) {
        super(Action.SEND_INFO);
        this.name = name;
        this.publicKey = publicKey;
    }

    public String getName() {
        return name;
    }

    public PGPPublicKey getPublicKey() {
        return publicKey;
    }
}
