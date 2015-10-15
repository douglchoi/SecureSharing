package cecs.secureshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import cecs.secureshare.R;
import cecs.secureshare.connector.SendImageAsyncTask;

/**
 * Custom activity that launches the camera and send the image to the device
 * Created by Douglas on 10/6/2015.
 */
public class SendImageActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener{

    private static final int SEND_IMAGE_REQUEST = 1;

    private String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // device stored in the extra bundle
        deviceAddress = getIntent().getStringExtra("deviceAddress");

        // Initialize camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, SEND_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEND_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");

            // get the image
            Uri imageUri = data.getData();

            // send the image
            // SendImageAsyncTask sendImageTask = new SendImageAsyncTask(this, deviceAddress, image);
            // sendImageTask.execute();

            Intent serviceIntent = new Intent(this, FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.FILE_URL, imageUri);
            serviceIntent.putExtra(FileTransferService.HOST_DEVICE_ADDRESS, deviceAddress);
            startService(serviceIntent);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        new SendImageAsyncTask(this, deviceAddress).execute();
    }
}
