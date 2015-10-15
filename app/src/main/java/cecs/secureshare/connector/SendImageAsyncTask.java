package cecs.secureshare.connector;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import cecs.secureshare.R;

/**
 * An asynchronous task to send an image
 * Created by Douglas on 9/30/2015.
 */
public class SendImageAsyncTask extends AsyncTask {

    private Context context;
    private String host;

    /**
     * @param context
     * @param host
     */
    public SendImageAsyncTask(Context context, String host) {
        this.context = context;
        this.host = host;
    }

    @Override
    protected String doInBackground(Object[] params) {
        try {
            ServerSocket serverSocket = new ServerSocket(R.integer.port);
            Socket client = serverSocket.accept();

            InputStream is = client.getInputStream();

            Log.d("Info", "Received data from device");

            serverSocket.close();
        } catch (IOException e) {
            Log.d("Info", e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}
