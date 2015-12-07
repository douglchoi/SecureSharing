package cecs.secureshare.connector.host;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cecs.secureshare.GroupViewActivity;
import cecs.secureshare.connector.client.JoinGroupService;
import cecs.secureshare.groupmanagement.GroupManager;


/**
 * An async task that creates a server socket and accepts multiple clients.
 * This task is created by the host.
 * Created by Douglas on 12/5/2015.
 */
public class AcceptGroupMemberAsyncTask extends AsyncTask<Void, Void, Integer> {

    public final String TAG = "AcceptGroupMember";

    private GroupViewActivity groupViewActivity;
    private boolean hostActive;

    /**
     * @param groupViewActivity
     */
    public AcceptGroupMemberAsyncTask(GroupViewActivity groupViewActivity) {
        this.groupViewActivity = groupViewActivity;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        hostActive = true;
        try {
            while(isHostActive()) {
                // New client wants to connect
                ServerSocket serverSocket = new ServerSocket(JoinGroupService.HOST_PORT);
                Socket clientSocket = serverSocket.accept();

                // initialize a new thread to handle communication with the client
                ClientConnectionThread clientConn = new ClientConnectionThread(clientSocket, serverSocket, groupViewActivity);
                clientConn.start();
            }
        } catch (IOException e){
            Log.d(TAG, e.getLocalizedMessage(), e);
        }
        return GroupManager.getInstance().getGroupSize();
    }

    /**
     * While host is active, this task will try to accept incoming connections
     * @return
     */
    public boolean isHostActive() {
        return hostActive;
    }

    /**
     * While host is active, this task will try to accept incoming connections
     * @param hostActive
     */
    public void setHostActive(boolean hostActive) {
        this.hostActive = hostActive;
    }
}
