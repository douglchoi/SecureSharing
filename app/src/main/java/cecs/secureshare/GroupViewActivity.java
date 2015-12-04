package cecs.secureshare;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cecs.secureshare.adapters.GroupMemberListAdapter;
import cecs.secureshare.adapters.PeerListAdapter;
import cecs.secureshare.security.GroupMember;

/**
 * This is the view where all members of the group can see each other
 */
public class GroupViewActivity extends AppCompatActivity implements View.OnClickListener {

    private SendFileFragment sendFileFragment;
    private GroupMemberListAdapter mGroupMemberListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);

        // Attach the group member list to the ListView adapter
        final ListView groupMemberListView = (ListView) findViewById(R.id.group_member_list_view);
        mGroupMemberListAdapter = new GroupMemberListAdapter(this, R.layout.simple_list_view_item, new ArrayList<GroupMember>());
        groupMemberListView.setAdapter(mGroupMemberListAdapter);

        // add a click handler for a group member
        groupMemberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // the selected group member
                final GroupMember groupMember = (GroupMember) groupMemberListView.getAdapter().getItem(position);
                // do something with the selected member
            }
        });

        // Button handlers
        Button shareFileButton = (Button) findViewById(R.id.share_file_button);
        Button disconnectButton = (Button) findViewById(R.id.disconnect_button);
        shareFileButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);

        // Popup for sending files
        sendFileFragment = (SendFileFragment) getFragmentManager().findFragmentById(R.id.fragment_send_file);
        sendFileFragment.getView().setVisibility(View.GONE);

        // TODO: This is for testing
        List<GroupMember> testGroupMembers = new ArrayList<GroupMember>();
        for (int i = 0; i < 10; i++) {
            GroupMember groupMember = new GroupMember();
            groupMember.setName("Group member " + i);
            testGroupMembers.add(groupMember);
        }
        mGroupMemberListAdapter.addAll(testGroupMembers);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.share_file_button:
                sendFileFragment.getView().setVisibility(View.VISIBLE);
                break;
            case R.id.disconnect_button:
                break;
        }
    }
}
