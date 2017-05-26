package experian.mobilesdk;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Firebase Messaging is the Google API for handling Push Notifications on Android.
 * This class extends the base Messaging Services.
 */
public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";


    //This event fires when a Push Notification is received from Firebase and the application is running or in the background.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        //Parses the Push Notification data and creates a JSON object from it.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "EMSMessage data payload: " + remoteMessage.getData());
            Map<String, String> kvp = remoteMessage.getData();
            JSONObject data = new JSONObject();
            for (Map.Entry<String, String> entry : kvp.entrySet())
            {
                try {
                    data.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Calls the notifyPush message on the EMSSDK with the data from the push.
            EMSMobileSDK.Default().notifyPush(getApplicationContext(), data);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "EMSMessage Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }

    //Simple send notification method. -- not being used.
    private void sendNotification(String messageBody) {
        Log.d(TAG, "EMSMessage Notification Body: " + messageBody);

    }
}