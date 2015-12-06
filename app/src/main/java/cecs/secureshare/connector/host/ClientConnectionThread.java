package cecs.secureshare.connector.host;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.acl.Group;

import cecs.secureshare.GroupViewActivity;
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

    private PrintWriter out = null;
    private BufferedReader in = null;

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
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // accept client information
            String clientInfo = readFromClient();

            // add it to the global list of group members
            GroupMember groupMember = new GroupMember(this);
            groupMember.setName(clientInfo);
            GroupManager.getInstance().addGroupMember(clientSocket.getInetAddress().toString(), groupMember);

            // update the UI
            groupViewActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    groupViewActivity.updateView();
                }
            });

            // all further messages
            while (running) {

            }
        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * This is a blocking I/O
     * @return
     */
    private String readFromClient() {
        try {
            String message = "";
            int charsRead = 0;
            char[] buffer = new char[BUFFER_SIZE];
            while ((charsRead = in.read(buffer)) != -1) {
                message += new String(buffer).substring(0, charsRead);
            }
            return message;
        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
