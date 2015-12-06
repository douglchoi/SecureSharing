package cecs.secureshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import cecs.secureshare.adapters.GroupMemberListAdapter;
import cecs.secureshare.connector.BroadcastReceiveListener;
import cecs.secureshare.connector.WiFiDirectBroadcastReceiver;
import cecs.secureshare.connector.client.FileTransferService;
import cecs.secureshare.connector.client.JoinGroupService;
import cecs.secureshare.connector.host.AcceptGroupMemberAsyncTask;
import cecs.secureshare.groupmanagement.GroupManager;
import cecs.secureshare.groupmanagement.GroupMember;

/**
 * This is the view where all members of the group can see each other
 */
public class GroupViewActivity extends AppCompatActivity implements BroadcastReceiveListener,
        WifiP2pManager.ConnectionInfoListener, View.OnClickListener {

    private static final int SEND_FILE_REQUEST = 2;

    private SendFileFragment sendFileFragment;
    private GroupMemberListAdapter mGroupMemberListAdapter;

    // Wifi components
    private WifiP2pManager mWifiManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    // Buttons
    private Button shareFileButton;
    private Button disconnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);

        // Attach the group member list to the ListView adapter
        final ListView groupMemberListView = (ListView) findViewById(R.id.group_member_list_view);
        mGroupMemberListAdapter = new GroupMemberListAdapter(this, R.layout.simple_list_view_item, new ArrayList<GroupMember>());
        groupMemberListView.setAdapter(mGroupMemberListAdapter);

        // Button handlers
        shareFileButton = (Button) findViewById(R.id.share_file_button);
        disconnectButton = (Button) findViewById(R.id.disconnect_button);
        shareFileButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);

        // Popup for sending files
        sendFileFragment = (SendFileFragment) getFragmentManager().findFragmentById(R.id.fragment_send_file);
        sendFileFragment.getView().setVisibility(View.GONE);

        initiateWifiDirect();
    }

    /**
     * Attach all the event listeners for Wifi activity
     */
    public void initiateWifiDirect() {
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

        mWifiManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(GroupViewActivity.this, "Peers can now connect to you.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(GroupViewActivity.this, "Please check if your Wifi is turned on", Toast.LENGTH_SHORT).show();
            }
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
                mWifiManager.requestPeers(mChannel, null);
            }
        } else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Update ui to show the wifip2p status
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // P2p wifi is enabled...
                enableFileSharing();
            } else {
                // P2p wifi is disabled...
                disableFileSharing();
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mWifiManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                mWifiManager.requestConnectionInfo(mChannel, this);
            } else {
                mWifiManager.removeGroup(mChannel, null);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Get the information about this device
            WifiP2pDevice myDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo
        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            new AcceptGroupMemberAsyncTask(this).execute();
            Toast.makeText(GroupViewActivity.this, "Host accepted connection... Need to accept a server thread.", Toast.LENGTH_SHORT).show();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            Intent serviceIntent = new Intent(this, JoinGroupService.class);
            serviceIntent.putExtra(JoinGroupService.HOST_DEVICE_ADDRESS, groupOwnerAddress);
            startService(serviceIntent);
            Toast.makeText(GroupViewActivity.this, "Client connected... Need to make a thread to connect to the group owner", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.share_file_button:
                // code for the device connecting to the group owner
                // Opens file browser. Then calls onActivityResult to send selected file to owner
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, SEND_FILE_REQUEST);

                break;
            case R.id.disconnect_button:
                break;
        }
    }

    /**
     * OnActivityResult is returned after the file is chosen
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_FILE_REQUEST ) {
            Toast.makeText(GroupViewActivity.this, "Sending image...", Toast.LENGTH_SHORT).show();

            // get the image
            Uri imageUri = data.getData();

            // TODO: 1. encrypt file in imageUri
            //       2. send file to host?


            Toast.makeText(GroupViewActivity.this, "Image sent!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update the UI
     */
    public void updateView() {
        Collection<GroupMember> groupMembers = GroupManager.getInstance().getGroupMembers().values();
        mGroupMemberListAdapter.clear();
        mGroupMemberListAdapter.addAll(groupMembers);
        mGroupMemberListAdapter.notifyDataSetChanged();
    }

    /**
     * Disable all the buttons
     */
    public void disableFileSharing() {
        sendFileFragment.getView().setVisibility(View.GONE);
        disconnectButton.setEnabled(false);
        shareFileButton.setEnabled(false);
    }

    /**
     * Enable all the buttons
     */
    public void enableFileSharing() {
        disconnectButton.setEnabled(true);
        shareFileButton.setEnabled(true);
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

}
