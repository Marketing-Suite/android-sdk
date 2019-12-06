package com.cheetahdigital.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import experian.mobilesdk.EMSIntents;
import experian.mobilesdk.EMSMobileSDK;

import static android.content.Context.NOTIFICATION_SERVICE;

public class SpecialReceiver extends BroadcastReceiver {

    //ID's used by the  notification PI in Android
    static final int NOTIFICATION_ID = 109011;
    static final String NOTIFICATION_TAG = "EMSNotification";
    static final String TAG = "EMS:NotificationReceive";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(context.getPackageName() + EMSIntents.EMS_SHOW_NOTIFICATION)) {
            Object data = intent.getExtras().get("data");
            if (data != null) {
                RemoteMessage message = (RemoteMessage) data;
                JSONObject payload = new JSONObject(message.getData());
                // Display the notification however you want
                displayNotification(context, payload);
            }
        } else if (intent.getAction().equals(context.getPackageName() + EMSIntents.EMS_OPEN_NOTIFICATION)) {
            EMSMobileSDK.Default().pushNotificationRegisterOpen(context, intent);
            // launch app
            try {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                if (launchIntent == null)
                    Log.d(TAG, "Unable to find launch intent");
                launchIntent.putExtra("EMS_OPEN_FROM_NOTIFICATION", true);
                context.startActivity(launchIntent);
                Log.d(TAG,"Leaving Receiver: " + launchIntent.getClass().toString());
            }
            catch (Exception ex)
            {
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    //This method is called whenever a Push Notification comes in.
    void displayNotification(Context ctx, JSONObject data){
        String title = "";
        String body = "";
        String channelId = ctx.getString(experian.mobilesdk.R.string.default_notification_channel_id);
        String channelName = ctx.getString(experian.mobilesdk.R.string.default_notification_channel_name);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx,channelId);

        // set default icon
        mBuilder.setSmallIcon(experian.mobilesdk.R.drawable.notification_icon);

        //Attempts to extract the tile from the Push Notification.
        if (data.has("title")){
            try {
                title = data.getString("title");
                mBuilder.setContentTitle(title);
            } catch (JSONException e) {
                mBuilder.setContentTitle("");
            }
        }

        //Attempts to extract the tile from the Push Notification.
        if (data.has("body")) {
            try {
                body = data.getString("body");
                mBuilder.setContentText(body);
            } catch (JSONException e) {
                mBuilder.setContentText("");
            }
        }

        Intent resultIntent =  new Intent(ctx.getPackageName() + EMSIntents.EMS_OPEN_NOTIFICATION);
        try {
            resultIntent.putExtra("ems_open",data.getString("ems_open"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(ctx, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotifyMgr =  (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(body);

            mNotifyMgr.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotifyMgr.notify(NOTIFICATION_TAG, NOTIFICATION_ID, mBuilder.build());
    }
}
