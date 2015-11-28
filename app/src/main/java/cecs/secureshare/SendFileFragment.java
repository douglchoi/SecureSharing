package cecs.secureshare;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cecs.secureshare.R;
import cecs.secureshare.connector.FileTransferAsyncTask;
import cecs.secureshare.connector.FileTransferService;

/**
 * Fragment for sending a file
 * Created by Douglas on 10/17/2015.
 */
public class SendFileFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final int SEND_FILE_REQUEST = 2;

    private View mContentView = null;
    private WifiP2pInfo wifiInfo = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.send_image, null);
        mContentView.findViewById(R.id.send_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // code for the device connecting to the group owner
                // Opens file browser. Then calls onActivityResult to send selected file to owner
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, SEND_FILE_REQUEST);
            }
        });
        return mContentView;
    }

    /**
     * This is called when the connection is ready to be used. The connection via ServerSockets
     * is created here.
     * @param info
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        // this is the current established connection
        wifiInfo = info;

        // show this fragment for data transfer controls
        getView().setVisibility(View.VISIBLE);

        TextView statusLabel = (TextView) mContentView.findViewById(R.id.status_label);

        // After group negotiation, we assign the group owner as the file server.
        if (info.groupFormed && info.isGroupOwner) {
            // hide the send button
            mContentView.findViewById(R.id.send_image_button).setVisibility(View.GONE);

            // code for the group owner. Starts an AsyncTask and waits for the data
            new FileTransferAsyncTask(getActivity(), statusLabel).execute();
        } else {
            // enable the send button
            mContentView.findViewById(R.id.send_image_button).setVisibility(View.VISIBLE);
        }
    }

    /**
     * OnActivityResult is returned after the image is taken
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        TextView statusLabel = (TextView) mContentView.findViewById(R.id.status_label);
        statusLabel.setText("Sending image...");

        if (requestCode == SEND_FILE_REQUEST ) {
            // get the image
            Uri imageUri = data.getData();

            // send the data
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.FILE_URL, imageUri.toString());
            serviceIntent.putExtra(FileTransferService.HOST_DEVICE_ADDRESS, wifiInfo.groupOwnerAddress.getHostAddress());
            getActivity().startService(serviceIntent);

            // update the label
            statusLabel.setText("Image sent!");
        }
    }

    /**
     * Resets this fragment
     */
    public void resetView() {
        this.getView().setVisibility(View.GONE);
        TextView statusLabel = (TextView) mContentView.findViewById(R.id.status_label);
        statusLabel.setText("");
    }
}
