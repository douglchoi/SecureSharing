package cecs.secureshare.connector.client;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Printer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Service for a client to connect to the group host.
 * Created by Douglas on 12/5/2015.
 */
public class JoinGroupService extends IntentService {

    public static final String TAG = "JoinGroupService";
    public static final String HOST_DEVICE_ADDRESS = "deviceAddress";
    public static final int HOST_PORT = 8988;
    public static final int SOCKET_TIMEOUT = 5000;

    public static final String SEND_FILE_ACTION = "send_file_action";
    public static final String RECEIVE_FILE_ACTION = "receive_file_action";

    private boolean running;
    private Socket socket;

    public JoinGroupService() {
        super("JoinGroupService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action.equals(SEND_FILE_ACTION)) {
            // client wants to send a file

        } else if (action.equals(RECEIVE_FILE_ACTION)) {
            // client wants to receive file

        }

        return START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        String hostDeviceAddress = intent.getExtras().getString(HOST_DEVICE_ADDRESS);

        socket = new Socket();
        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostDeviceAddress, HOST_PORT)), SOCKET_TIMEOUT);
            Log.d(TAG, "Connected to host: " + hostDeviceAddress);

            // send my info
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.write("message");

            running = true;
            while(running) {

            }

        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        } finally {
            // Do something?
        }
    }
}
