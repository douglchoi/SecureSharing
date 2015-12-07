package cecs.secureshare.connector.messages;

/**
 * A message that store the information about this group memeber
 * Created by Douglas on 12/6/2015.
 */
public class SendInfoMessage extends Message {

    private String name;
    private byte[] encodedPublicKeyRing;

    public SendInfoMessage() {
        super(Action.SEND_INFO);
    }

    /**
     * @param name
     * @param encodedPublicKeyRing
     */
    public SendInfoMessage(String name, byte[] encodedPublicKeyRing) {
        super(Action.SEND_INFO);
        this.name = name;
        this.encodedPublicKeyRing = encodedPublicKeyRing;
    }

    public String getName() {
        return name;
    }

    public byte[] getEncodedPublicKeyRing() {
        return encodedPublicKeyRing;
    }
}
