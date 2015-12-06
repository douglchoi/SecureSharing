package cecs.secureshare.connector.client;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
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

    public JoinGroupService() {
        super("JoinGroupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        String hostDeviceAddress = intent.getExtras().getString(HOST_DEVICE_ADDRESS);

        Socket socket = new Socket();

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostDeviceAddress, HOST_PORT)), SOCKET_TIMEOUT);

            Log.d(TAG, "Connected to host: " + hostDeviceAddress);
        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        } finally {
            // Do something?
        }
    }
}
