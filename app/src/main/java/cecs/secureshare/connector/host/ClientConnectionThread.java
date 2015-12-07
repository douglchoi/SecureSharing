package cecs.secureshare.connector.host;

import android.util.Log;

import org.spongycastle.openpgp.PGPPublicKey;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;

import cecs.secureshare.GroupViewActivity;
import cecs.secureshare.connector.messages.Message;
import cecs.secureshare.connector.messages.SendFileMessage;
import cecs.secureshare.connector.messages.SendInfoMessage;
import cecs.secureshare.groupmanagement.GroupManager;
import cecs.secureshare.groupmanagement.GroupMember;

/**
 * The host will create this new thread for each client connection
 * Created by Douglas on 12/5/2015.
 */
public class ClientConnectionThread extends Thread {

    public static final String TAG = "ClientConnectionThread";

    private static final int BUFFER_SIZE = 2048;

    private boolean running;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private GroupViewActivity groupViewActivity;

    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;

    /**
     * @param clientSocket
     */
    public ClientConnectionThread(Socket clientSocket, ServerSocket serverSocket, GroupViewActivity groupViewActivity) {
        running = true;
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;
        this.groupViewActivity = groupViewActivity;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());


            // all further messages
            while (running) {
                // receive a message. This is blocking I/O
                Message message = Message.readInputStream(in);

                // determine action based on message type
                switch (message.getAction()) {
                    case SEND_INFO: // accept client information
                        SendInfoMessage infoMessage = (SendInfoMessage) message;

                        // add it to the global list of group members
                        GroupMember groupMember = new GroupMember(this, infoMessage.getEncodedPublicKeyRing());
                        groupMember.setName(infoMessage.getName());

                        GroupManager.getInstance().addGroupMember(clientSocket.getInetAddress().toString(), groupMember);

                        // update the UI
                        groupViewActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                groupViewActivity.updateView();
                            }
                        });
                        break;

                    case SEND_FILE:
                        // do something with received file... send it to all clients
                        byte[] fileByteArray = ((SendFileMessage) message).getFileByteArray();

                        byte[] decryptedByteFileArray = new byte[fileByteArray.length]; // TODO: decrypt the fileByteArray

                        // loop through connected devices
                        for (GroupMember otherMembers : GroupManager.getInstance().getGroupMembers().values()) {
                            // don't send to file to myself
                            if (otherMembers.getClientConn() != this) {
                                byte[] encryptedFileByteArray = new byte[fileByteArray.length]; // TODO: encrypt fileByteArray with host's public key
                                PGPPublicKey publicKey = otherMembers.getPublicKey(); // TODO: this the group member's public key

                                // sends the file
                                otherMembers.getClientConn().sendFileToClient(encryptedFileByteArray);
                            }
                        }

                        break;
                }
            }
        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Sends file byte array to the connected peer
     * @param fileByteArray
     */
    public void sendFileToClient(byte[] fileByteArray) {
        SendFileMessage sendFileMessage = new SendFileMessage(fileByteArray);
        sendFileMessage.writeToOutputStream(out);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
