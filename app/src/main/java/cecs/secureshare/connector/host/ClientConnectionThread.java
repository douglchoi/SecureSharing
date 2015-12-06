package cecs.secureshare.connector.host;

import java.net.Socket;

/**
 * The host will create this new thread for each client connection
 * Created by Douglas on 12/5/2015.
 */
public class ClientConnectionThread extends Thread {

    private boolean running;
    private Socket clientSocket;

    /**
     * @param clientSocket
     */
    public ClientConnectionThread(Socket clientSocket) {
        running = true;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        while (running) {
            // do stuff
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
