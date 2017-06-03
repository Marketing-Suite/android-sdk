package experian.mobilesdk;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
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
    private String APIPostEndPoint;
    private String PRID;
    private boolean startNotified = false;
    private IEMSCallback PushCallback;

    //ID's used by the  notification PI in Android
    static final int NOTIFICATION_ID = 109011;
    static final String NOTIFICATION_TAG = "EMSNotification";

    private EMSService mBoundService;

    public static EMSMobileSDK Default() {
        return instance;
    }

    private EMSMobileSDK() {
    }

    /**
     * Initialization of the SDK.  This method should be called before any other calls to the EMS SDK.
     * @param ctx  the application context
     * @param appIntent the application intent - used for registering app open on push notification received
     * @param AppID the CCMP application ID for the application
     * @param CustomerID the CCMP customer ID for the application
     */
    public void init(Context ctx, Intent appIntent, String AppID, int CustomerID) {
        this.init(ctx, appIntent, AppID, CustomerID, Region.NORTH_AMERICA);
    }

    /**
     * Initialization of the SDK.  This method should be called before any other calls to the EMS SDK.
     * @param ctx  the application context
     * @param appIntent the application intent - used for registering app open on push notification received
     * @param AppID the CCMP application ID for the application
     * @param CustomerID the CCMP customer ID for the application
     * @param region the Region for your instance of CCMP
     */
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
                this.PushCallback.Callback(responseData.toString());
            }
        }

        this.AppID = AppID;
        this.CustomerID = CustomerID;
        this.Region = region;

        switch (region){
            case NORTH_AMERICA_SANDBOX:
                this.EndPoint = "http://cs.sbox.eccmp.com";
                this.APIPostEndPoint = "http://cs.sbox.eccmp.com/ats";
                break;
            case EMEA:
                this.EndPoint = "https://xts.ccmp.eu";
                this.APIPostEndPoint = "https://ats.ccmp.eu/ats";
                break;
            case JAPAN:
                this.EndPoint = "https://xts.ccmp.experian.co.jp";
                this.APIPostEndPoint = "https://ats.ccmp.experian.co.jp/ats";
                break;
            case NORTH_AMERICA:
            default:
                this.EndPoint = "https://xts.eccmp.com";
                this.APIPostEndPoint = "https://ats.eccmp.com/ats";
                break;
        }
    }

    /**
     * Registers a callback to be called for any incoming Push notifications
     * @param pushCallback function to be called when a Push notification is received
     */
    public void RegisterPushCallback(IEMSCallback pushCallback){
        RegisterPushCallback(pushCallback, 0);
    }

    /**
     * Registers a push calback with a simple callback method and/or an activity class.
     * In the case of an activity class, the activity will be started.
     * In both cases, a Notification ivon will be displayed and handled by the SDK.
     * @param pushCallback function to be called when a Push notification is received
     * @param notificationIcon points to a resource for Push Notifications.
     */
    public void RegisterPushCallback(IEMSCallback pushCallback, int notificationIcon){

        // save callback
        PushCallback = pushCallback;

        // save in preferences to is available to service
        setNotificationIcon(notificationIcon);
    }

    /*
    public void getRegistrationToken(IEMSCallback callback) {
            String url = EndPoint + "/xts/getregistrationtoken/cust/" + CustomerID + "/application/" + AppID + "/prid/" + PRID;
            QueueMessage(url, "GET", "application/json", "", callback);
    }
    */

    /**
     * Performs API Post and notifies callback
     * @param formId id of form to post
     * @param data array of key/value pairs
     * @param callback function to be called when post is received
     */
    public void APIPost(int formId, ArrayMap<String, String> data, IEMSCallback callback) {

        // core values
        String body = "cr=" + CustomerID + "&fm=" + formId;

        // then params
        for (Map.Entry<String, String> param : data.entrySet())
            body += "&" + param.getKey() + "=" + param.getValue();

        QueueMessage(APIPostEndPoint + "/post.aspx", "POST", "application/x-www-form-urlencoded", body, callback);
    }

    //The Notification tap is handled here with the calling intent.
    void notifyPushNotificationTap(Context ctx, Intent intent){

        NotificationManager mNotifyMgr =  (NotificationManager)  ctx.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);

        //If the ems_open property is set, CCMP is notified that the application was opened by way of a Notification Tap.
        if(intent.hasExtra("ems_open")){
            Log.d("","App Open URL: " + intent.getStringExtra("ems_open"));

            class appOpen implements IEMSCallback{

                @Override
                public void Callback(String dataResponse) {
                    Log.d("", "App was opened!");
                }
            }

            QueueMessage(intent.getStringExtra("ems_open"),"GET", "application/json", "",new appOpen());

        }

        // launch app
        Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        ctx.startActivity(launchIntent);
    }

    //This method is called whenever a Push Notification comes in.
    void notifyPush(Context ctx, JSONObject data){

        // get icon
        int notificationIcon = getNotificationIcon(ctx);

        //Constructs the Notification area message and displays it if notification icon set
        if (notificationIcon > 0){

            Builder mBuilder = new Builder(ctx);

            // set icon
            mBuilder.setSmallIcon(notificationIcon);

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

            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(ctx, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotifyMgr =  (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);

            mNotifyMgr.notify(NOTIFICATION_TAG, NOTIFICATION_ID, mBuilder.build());

        }

        //Calls callback method.
        if (this.PushCallback != null){
            this.PushCallback.Callback(data.toString());
        }

    }

    //The Device token is set.
    // It checks to see if the token has changed or if the PRID is not set.
    // If either condition is true, the SDK attempts to register a PRID with the token.
    void setDeviceToken(Context ctx, String DeviceToken){

        this.Token = DeviceToken;

        if (DeviceToken != null){

            String oldToken = "";
            Readable dataReader = new AndroidIO();
            Persistable dataWriter = new AndroidIO();

            try {
                oldToken = dataReader.readToken(ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!DeviceToken.equals(oldToken) || this.PRID == null || this.PRID.equals("")){

                try {
                    dataWriter.persistToken(DeviceToken, ctx);
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
                    public void Callback(String dataResponse) {

                        try {

                            if (!dataResponse.equals("Error")) {
                                JSONObject jObj = new JSONObject(dataResponse);
                                EMSMobileSDK.Default().setPRID(jObj.getString("Push_Registration_Id"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


                String verb = "PUT";
                if (oldToken.equals("") || this.PRID == null || this.PRID.equals("") ){
                    verb = "POST";
                }

                QueueMessage(DeviceRegURL,verb, "application/json", body, new deviceRegistered());
            }

        }

    }

    //Allows users to queue messages.
    void QueueMessage(String URL , String method, String contentType, Object body){
        this.QueueMessage(URL, method, contentType, body, null);
    }

    //Allows users to queue messages with a callback.
    void QueueMessage(String URL , String method, String contentType, Object body, IEMSCallback Callback){

        EMSMessage msg = new EMSMessage();
        msg.setBody(body);
        msg.setUrl(URL);
        msg.setMethod(method);
        msg.setContentType(contentType);
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
            EMSMobileSDK.Default().setDeviceToken(mBoundService.getApplicationContext(),  token);
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

    /**
     * Returns the PRID created by CCMP to identify this device
     * @return returns the PRID created by CCMP to identify this device
     */
    public String getPRID() {
        return PRID;
    }

    private void setPRID(String PRID) {
        try {
            Persistable dataWriter = new AndroidIO();
            dataWriter.persistPRID(PRID, this.context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.PRID = PRID;
    }

    /**
     * Returns the Device Token assigned by Google Services
     * @return returns the device token assigned by Google Services
     */
    public String getToken() {
        return Token;
    }

    private void setToken(String token) {
        Token = token;
    }

    /**
     * Returns the CCMP Application ID used to initialize the EMS SDK
     * @return Returns the CCMP Application ID used to initialize the EMS SDK
     */
    public String getAppID() {
        return AppID;
    }

    /**
     * Returns the CCMP Customer ID used to initialize the EMS SDK
     * @return Returns the CCMP Customer ID used to initialize the EMS SDK
     */
    public int getCustomerID(){ return this.CustomerID;}

    /**
     * Returns the CCMP Region used to initialize the EMS SDK
     * @return Returns the CCMP Region used to initialize the EMS SDK
     */
    public String getRegion() {return this.Region == null ? "" : this.Region.toString();}

    private void setNotificationIcon(int notificationIcon) {
        SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("NotificationIcon", notificationIcon);
        editor.commit();
    }

    private int getNotificationIcon(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
        return sharedPref.getInt("NotificationIcon", 0);
    }

}
