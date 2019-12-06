package experian.mobilesdk;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Firebase Messaging is the Google API for handling Push Notifications on Android.
 * This class extends the base Messaging Services.
 */
public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "EMS:MessagingService";


    /**
     * This event fires when a Push Notification is received from Firebase and the application is running or in the background.
     *
     * @param remoteMessage {@link RemoteMessage} received from Firebase
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Parses the Push Notification data and creates a JSON object from it.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "EMSMessage data payload: " + remoteMessage.getData());
            JSONObject data = new JSONObject(remoteMessage.getData());
            Log.d(TAG, "EMSMessage JSON: " + data.toString());

            //Send Message to App via LocalBroadcastManager
            Intent intent = new Intent(getApplicationContext().getPackageName() + EMSIntents.EMS_PUSH_RECEIVED);
            intent.setPackage(getApplicationContext().getPackageName());
            intent.putExtra("data", remoteMessage);
            sendBroadcast(intent);

            //Send Message to Display Notification
            intent = new Intent(getApplicationContext().getPackageName() + EMSIntents.EMS_SHOW_NOTIFICATION);
            intent.setPackage(getApplicationContext().getPackageName());
            intent.putExtra("data", remoteMessage);
            sendBroadcast(intent);
        }
    }
}