package experian.mobilesdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * This class receives a Broadcast Notification from Android when the network state changes.
 * If the Messaging Service isn't started, then the service is started.
 */

public class ConnectionReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {

                Intent i = new Intent(".EMSService");
                i.setClass(context, EMSService.class);
                context.startService(i);

            }
        }
    }


}
