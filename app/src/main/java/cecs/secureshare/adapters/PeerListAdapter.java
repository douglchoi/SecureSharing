package cecs.secureshare.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.List;

import cecs.secureshare.R;

/**
 * List view adapter for list of WifiP2pDevices.
 * Created by Douglas on 9/24/2015.
 */
public class PeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

    Context mContext;
    int layoutResourceId;
    List<WifiP2pDevice> devices = null;

    /*
     * Creates an empty PeerListAdapter
     * @param mContext
     * @param layoutResourceId
     */
    public PeerListAdapter(Context mContext, int layoutResourceId, List<WifiP2pDevice> devices) {
        super(mContext, layoutResourceId, devices);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.devices = devices;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        WifiP2pDevice device = devices.get(position);

        // Display the devices name in the text view
        TextView textView = (TextView) convertView.findViewById(R.id.simple_list_view_item);
        textView.setText(device.deviceName);

        return convertView;
    }
}
