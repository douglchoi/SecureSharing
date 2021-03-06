package cecs.secureshare.connector;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import cecs.secureshare.R;

/**
 * Handles sending the file
 * Created by Douglas on 10/11/2015.
 */
public class FileTransferService extends IntentService {

    public static final String FILE_URL = "fileUrl";
    public static final String HOST_DEVICE_ADDRESS = "deviceAddress";
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final int HOST_PORT = 8988;
    public static final int SOCKET_TIMEOUT = 5000;

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {

            // location to write to
            String fileUri = intent.getExtras().getString(FILE_URL);

            // device address to receive from
            String host = intent.getExtras().getString(HOST_DEVICE_ADDRESS);

            Socket socket = new Socket();

            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, HOST_PORT)), SOCKET_TIMEOUT);

                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d("Info", e.toString());
                }
                FileTransferAsyncTask.copyFile(is, stream);
                Log.d("Info", "Data written");
            } catch (IOException e) {
                Log.e("Info", e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
