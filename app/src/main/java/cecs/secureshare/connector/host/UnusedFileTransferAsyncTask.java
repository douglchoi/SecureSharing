package cecs.secureshare.connector.host;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cecs.secureshare.connector.client.UnusedFileTransferService;

/**
 * A simple server socket that accepts incoming connections and writes some
 * data on the stream
 * Created by Douglas on 9/30/2015.
 */
public class UnusedFileTransferAsyncTask extends AsyncTask<Void, Void, String>{

    private final String TAG = "UnusedFileTransferAsyncTask";
    protected Context context;
    protected TextView statusLabel;

    /**
     * @param context
     */
    public UnusedFileTransferAsyncTask(Context context, TextView statusLabel) {
        this.context = context;
        this.statusLabel = statusLabel;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            // open a server socket
            ServerSocket serverSocket = new ServerSocket(UnusedFileTransferService.HOST_PORT);
            Socket client = serverSocket.accept();

            // write file to directory
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/secureshare-" + System.currentTimeMillis()
                    + ".jpg");

            File dirs = new File(f.getParent());
            if (!dirs.exists()) {
                dirs.mkdir();
            }
            f.createNewFile();

            // read data from client
            InputStream is = client.getInputStream();
            copyFile(is, new FileOutputStream(f));
            serverSocket.close();
            Log.d(TAG, "File sent...");
            return f.getAbsolutePath();

        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        statusLabel.setText("Awaiting image...");
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        statusLabel.setText("Transferring image...");
    }

    @Override
    protected void onPostExecute(String fileUri) {
        super.onPostExecute(fileUri);
        statusLabel.setText("File saved at " + fileUri);
    }

    /**
     * Writes input stream data into output stream
     * @param inputStream
     * @param outputStream
     * @return
     */
    public static boolean copyFile(InputStream inputStream, OutputStream outputStream) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("Info", e.toString());
            return false;
        }
        return true;
    }
}
