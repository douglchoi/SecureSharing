package cecs.secureshare.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Receive WiFi Broadcasts. Forwards the onReceive event to the listener.
 * Created by Douglas on 9/21/2015.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiveListener mReceiveListener;

    /**
     * @param manager
     * @param channel
     * @param mReceiveListener
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, BroadcastReceiveListener mReceiveListener) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mReceiveListener = mReceiveListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mReceiveListener.onReceive(context, intent);
    }
}