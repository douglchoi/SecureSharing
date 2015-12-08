package cecs.secureshare.connector.client;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import cecs.secureshare.connector.FileWriter;
import cecs.secureshare.connector.messages.Message;
import cecs.secureshare.connector.messages.SendFileMessage;
import cecs.secureshare.connector.messages.SendInfoMessage;
import cecs.secureshare.groupmanagement.PeerInfo;
import cecs.secureshare.security.CryptoManager;

/**
 * Service for a client to connect to the group host.
 * Created by Douglas on 12/5/2015.
 */
public class JoinGroupService extends IntentService {

    public static final String TAG = "JoinGroupService";
    public static final String HOST_DEVICE_ADDRESS = "deviceAddress";
    public static final int HOST_PORT = 8988;
    public static final int SOCKET_TIMEOUT = 20000;

    public static final String SEND_FILE_ACTION = "send_file_action";
    public static final String RECEIVE_FILE_ACTION = "receive_file_action";

    private boolean running;
    private Socket socket;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public JoinGroupService() {
        super("JoinGroupService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();

        if (SEND_FILE_ACTION.equals(action)) {
            // client wants to send a file

        } else if (RECEIVE_FILE_ACTION.equals(action)) {
            // client wants to receive file

        }

        return START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();

        String hostDeviceAddress = intent.getExtras().getString(HOST_DEVICE_ADDRESS);

        socket = new Socket();
        PeerInfo.getInstance().setCurrentConn(this);

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostDeviceAddress, HOST_PORT)), SOCKET_TIMEOUT);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send my info
            byte[] encodedPkr = CryptoManager.getInstance().getPublicKeyRing().getEncoded();
            SendInfoMessage clientInfoMessage = new SendInfoMessage("My name", encodedPkr);
            clientInfoMessage.writeToOutputStream(out);
            out.flush();

            // all further messages
            running = true;
            while (running) {
                // receive a message. This is blocking I/O
                Message message = Message.readInputStream(in);

                // determine action based on message type
                switch (message.getAction()) {
                    case SEND_INFO: // accept host information
                        SendInfoMessage infoMessage = (SendInfoMessage) message;
                        // set the public key from the host
                        PeerInfo.getInstance().setHostPublicKeys(infoMessage.getEncodedPublicKeyRing());
                        break;

                    case SEND_FILE:
                        // do something with received file from the host
                        byte[] fileByteArray = ((SendFileMessage) message).getFileByteArray();
                        // decrypt with secret key
                        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
                        CryptoManager.getInstance().decrypt(
                                new ByteArrayInputStream(fileByteArray),
                                decrypted,
                                CryptoManager.getInstance().getSecretKey(),
                                PeerInfo.getInstance().getHostSigningPublicKey());

                        // save to directory
                        FileWriter.writeFile(context, new ByteArrayInputStream(fileByteArray));  // for demo, also write the encrypted file
                        FileWriter.writeFile(context, new ByteArrayInputStream(decrypted.toByteArray())); // this is the actual decrypted file

                        Toast.makeText(context, "File received!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        } finally {
            // Do something?
        }
    }

    /**
     * Sends a file byte array to the host
     * @param fileByteArray
     */
    public void sendFileToHost(byte[] fileByteArray) {
        SendFileMessage sendFileMessage = new SendFileMessage(fileByteArray);
        sendFileMessage.writeToOutputStream(out);
    }
}
