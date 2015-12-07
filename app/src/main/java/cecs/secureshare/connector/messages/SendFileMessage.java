package cecs.secureshare.connector.messages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * A message containing files in a byte array
 * Created by Douglas on 12/6/2015.
 */
public class SendFileMessage extends Message {

    private byte[] fileByteArray;

    public SendFileMessage(byte[] fileByteArray) {
        super(Action.SEND_FILE);
        this.fileByteArray = fileByteArray;
    }

    public byte[] getFileByteArray() {
        return fileByteArray;
    }
}
