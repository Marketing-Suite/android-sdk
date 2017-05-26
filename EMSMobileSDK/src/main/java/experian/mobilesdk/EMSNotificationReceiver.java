package experian.mobilesdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

/**
 * This class receives Broadcast Notifications from Android whenever a Notification is tapped.
 * The Notification itself is created bby the EMSMobileSDK whenever a Push Notification is received.
 */

public class EMSNotificationReceiver extends BroadcastReceiver {

    public static final String EMS_NOTIFICATION_INTENT = "experian.mobilesdk.EMSNotificationReceiver.EMS_NOTIFICATION_INTENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(EMSNotificationReceiver.EMS_NOTIFICATION_INTENT)) {

            EMSMobileSDK.Default().notifyPushNotificationTap(context, intent);

        }
    }
}