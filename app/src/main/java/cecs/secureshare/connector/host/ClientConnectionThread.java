package cecs.secureshare.connector.host;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cecs.secureshare.GroupViewActivity;
import cecs.secureshare.connector.FileWriter;
import cecs.secureshare.connector.messages.Message;
import cecs.secureshare.connector.messages.SendFileMessage;
import cecs.secureshare.connector.messages.SendInfoMessage;
import cecs.secureshare.groupmanagement.GroupManager;
import cecs.secureshare.groupmanagement.GroupMember;
import cecs.secureshare.security.CryptoManager;

/**
 * The host will create this new thread for each client connection
 * Created by Douglas on 12/5/2015.
 */
public class ClientConnectionThread extends Thread {

    public static final String TAG = "ClientConnectionThread";

    private boolean running;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private GroupViewActivity groupViewActivity;
    private GroupMember groupMember;

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
                        this.groupMember = new GroupMember(this, infoMessage.getEncodedPublicKeyRing());
                        groupMember.setName(infoMessage.getName());

                        GroupManager.getInstance().addGroupMember(clientSocket.getInetAddress().toString(), groupMember);

                        // send the client host's public key
                        byte[] encodedPkr = CryptoManager.getInstance().getPublicKeyRing().getEncoded();
                        SendInfoMessage hostInfoMessage = new SendInfoMessage("Host", encodedPkr);
                        hostInfoMessage.writeToOutputStream(out);

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

                        // decrypt with host's key
                        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
                        CryptoManager.getInstance().decrypt(
                                new ByteArrayInputStream(fileByteArray),
                                decrypted,
                                CryptoManager.getInstance().getSecretKey(),
                                groupMember.getSigningPublicKey());

                        // just keep a copy on the host device
                        FileWriter.writeFile(groupViewActivity.getApplicationContext(), new ByteArrayInputStream(fileByteArray)); // for demo, also write the encrypted file
                        FileWriter.writeFile(groupViewActivity.getApplicationContext(), new ByteArrayInputStream(decrypted.toByteArray())); // this is the actual decrypted file

                        // loop through connected devices
                        for (GroupMember otherMembers : GroupManager.getInstance().getGroupMembers().values()) {
                            // don't send to file to myself
                            if (otherMembers.getClientConn() != this) {
                                // encrypt with client's public key
                                ByteArrayOutputStream reEncrypted = new ByteArrayOutputStream();
                                CryptoManager.getInstance().encrypt(new ByteArrayInputStream(decrypted.toByteArray()), reEncrypted, otherMembers.getPublicKey());
                                // sends the file
                                otherMembers.getClientConn().sendFileToClient(reEncrypted.toByteArray());
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
