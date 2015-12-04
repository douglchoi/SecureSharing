package cecs.secureshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cecs.secureshare.adapters.PeerListAdapter;
import cecs.secureshare.connector.BroadcastReceiveListener;
import cecs.secureshare.connector.WiFiDirectBroadcastReceiver;

/**
 * This activity lists the peers that a device can connect to. 
 */
public class FindPeerActivity extends AppCompatActivity implements BroadcastReceiveListener,
        WifiP2pManager.PeerListListener {

    WifiP2pManager mWifiManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    PeerListAdapter mPeerListAdapter;
    IntentFilter mIntentFilter;

    private WifiP2pInfo wifiP2pInfo;
    private boolean transferring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_peer);

        // Fetch and initialize the wifi p2p service
        mWifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mWifiManager, mChannel, this);

        // Register P2P actions
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Attach the peer list to the ListView adapter
        final ListView peerListView = (ListView) findViewById(R.id.peer_list_view);
        mPeerListAdapter = new PeerListAdapter(this, R.layout.simple_list_view_item, new ArrayList<WifiP2pDevice>());
        peerListView.setAdapter(mPeerListAdapter);

        // add a click handler for a peer in the list
        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // the selected device
                final WifiP2pDevice device = (WifiP2pDevice) peerListView.getAdapter().getItem(position);

                // create the connection configuration
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                // connect to the device
                mWifiManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // The devices are connected
                    }

                    @Override
                    public void onFailure ( int reason){
                        Toast.makeText(FindPeerActivity.this, "Failed to connect to " + device.deviceName + " [Reason = " + reason + "]", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Initiate peer discovery
        mWifiManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(int reasonCode) {}
        });
    }

    /**
     * Receive Wifi broadcasts
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an++
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mWifiManager != null) {
                mWifiManager.requestPeers(mChannel, this);
            }
        } else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Update ui to show the wifip2p status
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // P2p wifi is enabled...
            } else {
                // P2p wifi is disabled...
                resetViews();
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mWifiManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // devices are connected
                SendImageFragment fragment = (SendImageFragment) getFragmentManager().findFragmentById(R.id.fragment_send_image);
                mWifiManager.requestConnectionInfo(mChannel, fragment);
            } else {
                // devices were disconnected
                mWifiManager.removeGroup(mChannel, null);
                resetViews();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Get the information about this device
            WifiP2pDevice myDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // When peers change status, update UI
        mPeerListAdapter.clear();

        if (peers.getDeviceList().isEmpty()) {
            Toast.makeText(FindPeerActivity.this, "Could not find any nearby peers.", Toast.LENGTH_SHORT).show();
        } else {
            mPeerListAdapter.addAll(peers.getDeviceList());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // enable the receiver when app resumes
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // disable the receive when app is put into background
        unregisterReceiver(mReceiver);
    }

    /**
     * Hides parts of the activity if not connected
     */
    public void resetViews() {
        SendImageFragment sendImageFragment = (SendImageFragment) getFragmentManager().findFragmentById(R.id.fragment_send_image);
        sendImageFragment.resetView();
    }
}
