package cecs.secureshare.connector;

import android.content.Context;
import android.content.Intent;

/**
 * Implement this interface to handle receive events from any Broadcasts
 * Created by Douglas on 9/22/2015.
 */
public interface BroadcastReceiveListener {
    public void onReceive(Context context, Intent intent);
}
