package cecs.secureshare.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cecs.secureshare.R;
import cecs.secureshare.security.GroupMember;

/**
 * Created by Douglas on 12/3/2015.
 */
public class GroupMemberListAdapter extends ArrayAdapter<GroupMember>{

    Context mContext;
    int layoutResourceId;
    List<GroupMember> groupMembers = null;

    /*
     * Creates an empty PeerListAdapter
     * @param mContext
     * @param layoutResourceId
     */
    public GroupMemberListAdapter(Context mContext, int layoutResourceId, List<GroupMember> groupMembers) {
        super(mContext, layoutResourceId, groupMembers);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.groupMembers = groupMembers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        GroupMember groupMember = groupMembers.get(position);

        TextView textView = (TextView) convertView.findViewById(R.id.simple_list_view_item);
        textView.setText(groupMember.getName());

        return convertView;
    }
}
