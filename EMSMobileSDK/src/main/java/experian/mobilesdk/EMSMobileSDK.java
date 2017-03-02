package experian.mobilesdk;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.app.NotificationCompat.*;

/**
 * Created by Blaize Stewart on 1/17/2017.
 *
 * This is the main entry point for the SDK that is exposed to the User
 * It contains the initialization and the hooks for registering call back events.
 */
public class EMSMobileSDK {
    private static EMSMobileSDK instance = new EMSMobileSDK();

    private Context context;
    private String AppID;
    private int CustomerID;
    private String Token;
    private Region Region;
    private String EndPoint;
    private String PRID;
    private Readable dataReader;
    private Persistable dataWriter;


    private boolean startNotified = false;
    private IEMSCallback PushCallback;
    private Class  CallbackClass = null;
    private int CallbackIcon = 0;

    //ID's used by the  notification PI in Android
    static final int NOTIFICATION_ID = 109011;
    static final String NOTIFICATION_TAG = "EMSNotification";

    private EMSService mBoundService;

    public static EMSMobileSDK Default() {
        return instance;
    }

    private EMSMobileSDK() {
    }

    //North American is the Default Region.
    public void init(Context ctx, Intent appIntent, String AppID, int CustomerID) {
        this.init(ctx, appIntent, AppID, CustomerID, Region.NORTH_AMERICA);
    }

    //The main init method -- this is called by the application.
    public void init(Context ctx, Intent appIntent, String AppID, int CustomerID, Region region) {
        context = ctx;

        Intent intent = new Intent(context, EMSService.class);

        if (!this.isServiceRunning()){
            context.startService(intent);
        }

        try{
           context.bindService(intent, mConnection, 0);
        }
        catch(Exception ex){
            Log.d("EMSSDK", ex.getMessage());
        }

        Bundle extras = appIntent.getExtras();

        //The appIntent was sent from a push notifications from Google. Parse it and notify, then register app open.
        if (extras != null && appIntent.hasExtra("google.message_id")){

            JSONObject responseData = new JSONObject();

            for (String key : extras.keySet()) {
                try {
                    responseData.put(key, extras.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(this.PushCallback != null){
                this.PushCallback.Callback(responseData);
            }

            EMSMessage msg = new EMSMessage();

        }

        this.AppID = AppID;
        this.CustomerID = CustomerID;
        this.Region = region;


        switch (region){
            case NORTH_AMERICA_SANDBOX:
                this.EndPoint = "http://cs.sbox.eccmp.com";
                break;
            case EMEA:
                this.EndPoint = "https://xts.ccmp.eu";
                break;
            case JAPAN:
                this.EndPoint = "http://xts.ccmp.experian.co.jp";
                break;
            case NORTH_AMERICA:
            default:
                this.EndPoint = "https://xts.eccmp.com";
                break;
        }


        this.dataReader = new AndroidIO();
        this.dataWriter = new AndroidIO();

    }

    //Register a push callback with a simple callback method.
    public void RegisterPushCallback(IEMSCallback pushCallback){

        this.PushCallback = pushCallback;
        this.CallbackClass = null;
        this.CallbackIcon = 0;
    }

    //Registers a push calback with a simple callback method and/or an activity class.
    //In the case of an activity class, the activity will be started.
    // In both cases, a Notification ivon will be displayed and handled by the SDK.
    // The Icon points to a resource for Push Notifications.
    public void RegisterPushCallback(IEMSCallback pushCallback, Class ActivityClass, int Icon){
        this.PushCallback = pushCallback;
        this.CallbackClass = ActivityClass;
        this.CallbackIcon = Icon;

    }

    //The Notification tap is handled here with the calling intent.
    void notifyPushNotificationTap(Intent intent){

        NotificationManager mNotifyMgr =  (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);

        //If the ems_open property is set, CCMP is notified that the application was opened by way of a Notification Tap.
        if(intent.hasExtra("ems_open")){
            Log.d("","App Open URL: " + intent.getStringExtra("ems_open"));

            class appOpen implements IEMSCallback{
                @Override
                public void Callback(JSONObject dataResponse) {
                    Log.d("", "App was opened!");
                }
            }

            QueueMessage(intent.getStringExtra("ems_open"),"GET", "",new appOpen());

        }

        //If the CallbackClass is set, the SDK will attempt to start the activity.
        if (this.CallbackClass != null){

            Intent activityIntent = new Intent(context, this.CallbackClass);

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }

    //This method is called whenever a Push Notification comes in.
    void notifyPush(JSONObject data){

        //Constructs the Notification area message and displays it.
        if (this.CallbackIcon > 0){

            Builder mBuilder =   new Builder(context);

            mBuilder.setSmallIcon(this.CallbackIcon);

            //Attempts to extract the tile from the Push Notification.
            if (data.has("title")){
                try {
                    mBuilder.setContentTitle(data.getString("title"));
                } catch (JSONException e) {
                    mBuilder.setContentTitle("");
                }
            }

            //Attempts to extract the tile from the Push Notification.
            if (data.has("body")) try {
                mBuilder.setContentText(data.getString("body"));
            } catch (JSONException e) {
                mBuilder.setContentText("");
            }

            Intent resultIntent =  new Intent("experian.mobilesdk.EMSNotificationReceiver.EMS_NOTIFICATION_INTENT");

            try {
                resultIntent.putExtra("ems_open",data.getString("ems_open"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotifyMgr =  (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            mNotifyMgr.notify(NOTIFICATION_TAG, NOTIFICATION_ID, mBuilder.build());

        }

        //Calls callback method.
        if (this.PushCallback != null){
            this.PushCallback.Callback(data);
        }

    }

    //The Device token is set.
    // It checks to see if the token has changed or if the PRID is not set.
    // If either condition is true, the SDK attempts to register a PRID with the token.
    void setDeviceToken(String DeviceToken){

        this.Token = DeviceToken;

        if (DeviceToken != null){

            String oldToken = "";

            try {
                oldToken = dataReader.readToken(this.context);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!DeviceToken.equals(oldToken) || this.PRID == null || this.PRID.equals("")){

                try {
                    this.dataWriter.persistToken(DeviceToken, this.context);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String DeviceRegURL = EndPoint + "/xts/registration/cust/" + CustomerID + "/application/" + AppID + "/token/";


                this.Token = DeviceToken;
                String body = null;

                JSONObject payload = new JSONObject();

                try {
                    payload.put("DeviceToken", DeviceToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                body = payload.toString();

                class deviceRegistered implements IEMSCallback{
                    @Override
                    public void Callback(JSONObject dataResponse) {
                        try {
                            EMSMobileSDK.Default().setPRID(dataResponse.getString("Push_Registration_Id"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


                String verb = "PUT";
                if (oldToken.equals("") || this.PRID == null || this.PRID.equals("") ){
                    verb = "POST";
                }

                QueueMessage(DeviceRegURL,verb,body, new deviceRegistered());
            }

        }

    }

    //Allows users to queue messages.
    void QueueMessage(String URL , String method, Object body){
        this.QueueMessage(URL, method, body, null);
    }

    //Allows users to queue messages with a callback.
    void QueueMessage(String URL , String method, Object body, IEMSCallback Callback){

        EMSMessage msg = new EMSMessage();
        msg.setBody(body);
        msg.setUrl(URL);
        msg.setMethod(method);
        msg.setCallback(Callback);

        if (mBoundService != null){
            mBoundService.QueueMessage(msg);
        }
    }

    //Handles setting up the Message Service for backgrounding.
    private ServiceConnection mConnection = new ServiceConnection() {

        //Once the service is started, the device token is set from Firebase.
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((EMSService.EMSBinder) service).getService();
            String token = FirebaseInstanceId.getInstance().getToken();
            EMSMobileSDK.Default().setDeviceToken(token);
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    //Checks to see if the Message Service is up and running.
    private boolean isServiceRunning(){
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(EMSService.class)){
                return true;
            }
        }
        return false;
    }

    private boolean isStartNotified() {
        return startNotified;
    }

    public String getPRID() {
        return PRID;
    }

    private void setPRID(String PRID) {
        try {
            this.dataWriter.persistPRID(PRID, this.context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.PRID = PRID;
    }

    public String getToken() {
        return Token;
    }

    private void setToken(String token) {
        Token = token;
    }

    public String getAppID() {
        return AppID;
    }

    public int getCustomerID(){ return this.CustomerID;}

    public String getRegion() {return this.Region == null ? "" : this.Region.toString();}

}
