package experian.mobilesdk;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class receives a Broadcast Notification from Android when the device boots.
 * Subsequently, it starts the messaging service in the background which handles the Message Queue
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(".EMSService");
        i.setClass(context, EMSService.class);
        context.startService(i);

    }
}
