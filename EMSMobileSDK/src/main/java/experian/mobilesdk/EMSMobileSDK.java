package experian.mobilesdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Blaize Stewart on 1/17/2017.
 *
 * This is the main entry point for the SDK that is exposed to the User
 * It contains the initialization and the hooks for registering call back events.
 */
public class EMSMobileSDK {
    private static EMSMobileSDK instance = new EMSMobileSDK();

    //Static keys for SharedPreferences
    private static final String SHARED_PREFERENCES_NAME = "EMSMobileSDK";
    private static final String CDMS_PRID = "CDMS_PRID";
    private static final String CDMS_CUSTID = "CDMS_CUSTID";
    private static final String CDMS_APPID = "CDMS_APPID";
    private static final String CDMS_TOKEN = "CDMS_TOKEN";
    private static final String CDMS_REGION = "CDMS_REGION";
    private static final String TAG = "EMS:EMSMobileSDK";

    private Context context;
    private String appID;
    private int customerID;
    private String token;
    private Region region;
    private String prid;

    //Callback interfaces
    private IEMSPRIDCallback pridCallback;

    public static EMSMobileSDK Default() {
        return instance;
    }

    /**
     * @param callingContext the originating android.content.Context object which made this call.
     * @return returns the private shared preferences for the SDK
     */
    private SharedPreferences GetPrivateSharedPreferences(Context callingContext)
    {
        return callingContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     *
     * @return returns the private shared preferences for the SDK
     */
    private SharedPreferences GetPrivateSharedPreferences()
    {
        return GetPrivateSharedPreferences(this.context);
    }

    /**
     *
     * @return returns the registration endpoint consisting of
     * customer id and application id
     */
    private String RegistrationEndpoint()
    {
        return getRegion().getEndpoint() + "/xts/registration/cust/" + getCustomerID() + "/application/" + getAppID() + "/token";
    }

    /**
     * Returns the prid created by CCMP to identify this device
     * @param callingContext the originating android.content.Context object which made this call.
     * @return returns the prid created by CCMP to identify this device
     */
    public String getPRID(Context callingContext) {
        return GetPrivateSharedPreferences(callingContext).getString(CDMS_PRID, null);
    }

    /**
     * Returns the prid created by CCMP to identify this device
     * @return returns the prid created by CCMP to identify this device
     */
    public String getPRID() {
        return getPRID(this.context);
    }

    /**
     * Set the PRID locally, and if a callback is set fire it.
     * If the PRID is unchanged, this is considered a no-op,
     * and the callback will not be fired.
     *
     * @param prid new PRID to set in object instance
     *
     */
    private void setPRIDAndFirePRIDCallback(String prid) {
        String originalPRID = getPRID();
        // no-op
        if ( originalPRID != null && originalPRID.equals(prid)) {
            return;
        }

        SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(CDMS_PRID, prid);
        editor.apply();
        this.prid = prid;
        // Don't call the callback in the case that we're null'ing out the PRID
        if (prid != null && pridCallback != null) {
            pridCallback.onPRIDReceived(prid);
        }
    }

    /**
     * Returns the Device token assigned by Google Services
     * @return returns the device token assigned by Google Services
     */
    public String getToken()
    {
        return GetPrivateSharedPreferences().getString(CDMS_TOKEN, "");
    }

    /**
     * This method is called to set the Device token on Init or Refresh
     *
     * @param token The device token received from Google's Firebase
     *
     */
    public void setToken(String token) {
        String originalToken = getToken();
        if ( originalToken.equals(token) )
        {
            return;
        }
        // new or updated
        SaveRemoteTokenAndSetPRID(getPRID() == null);

        // set the token locally
        SharedPreferences.Editor editor = GetPrivateSharedPreferences().edit();
        editor.putString(CDMS_TOKEN, token);
        editor.apply();
        this.token = token;

    }

    /**
     * Store the device token in Marketing Suite, and depending on whether a PRID
     * has been assigned for this token, save the PRID locally
     *
     * @param pridIsAssigned boolean indicating whether a PRID has been assigned
     *                       via Marketing Suite for the device token being saved
     */
    private void SaveRemoteTokenAndSetPRID(boolean pridIsAssigned)
    {
        VolleySender.getInstance(context).addToRequestQueue(
            new EMSJSONObjectRequest(
                pridIsAssigned ? Request.Method.PUT : Request.Method.POST,
                RegistrationEndpoint(),
                TokenSubmissionJsonBody(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        setPRIDFromRemoteResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "Error submitting token registration request: " + error.getMessage());
                    }
                }
            )
        );
    }

    /**
     * Mark the token remotely as having been deactivated, indicating push
     * notifications are opted-out for this device.
     *
     */
    private void DeactivateRemoteToken()
    {
        VolleySender.getInstance(context).addToRequestQueue(
            new EMSStringRequest(
                    Request.Method.DELETE,
                    RegistrationEndpoint(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, String.format("Received response upon deactivating token: %s", response.toString()) );
                            setPRIDAndFirePRIDCallback(null);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i(TAG, String.format("Error submitting registration request.  Error message: %s. Error toString(): %s", error.getMessage(), error.toString() ));
                        }
                    }) {
                @Override
                public byte[] getBody() { return TokenSubmissionJsonBody().toString().getBytes(); }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            }
        );
    }

    /**
     * Create the JSON body containing the device token for submission
     * to Marketing Suite
     *
     * @return JSONObject containing the device token, with 'DeviceToken' as the
     * object key.
     */
    private JSONObject TokenSubmissionJsonBody()
    {
        JSONObject body = new JSONObject();
        try {
            body.put("DeviceToken", getToken());
        } catch (JSONException ex) {
            Log.d(TAG, "Unable to set device token in body");
        }
        return body;
    }


    /**
     *
     * After remote submission of a token, the response is returned as JSON,
     * and parsed here.  If the PRID is new, call {@link #setPRIDAndFirePRIDCallback(String)}
     * *
     * @param response JSON response containing PRID
     */
    private void setPRIDFromRemoteResponse(JSONObject response)
    {
        try {
            setPRIDAndFirePRIDCallback(response.getString("Push_Registration_Id"));
        } catch (JSONException ex) {
            Log.w(TAG, String.format("Error parsing response for PRID: %s, exception: %s", response.toString(), ex.getMessage()));
        }
    }

    /**
     * This method is called to set the Device token on Init or Refresh when
     * application context is required
     *
     * @param context
     * @param token
     *
     */
    public void setToken(Context context, String token) {
        SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
        String storedToken = sharedPref.getString(CDMS_TOKEN, "");
        if (!storedToken.equals(token)) {
            //Register this token and get PRID
            int method = Request.Method.POST;
            String storedPRID = getPRID();
            if (storedPRID != null) {
                //Update Device Registration - HTTP PUT
                method = Request.Method.PUT;
            }
            JSONObject body = new JSONObject();
            try {
                body.put("DeviceToken", token);
            } catch (JSONException ex) {
                Log.d(TAG, "Unable to set device token in body");
            }
            String url = getRegion().getEndpoint() + "/xts/registration/cust/" + getCustomerID() + "/application/" + getAppID() + "/token";
            EMSJSONObjectRequest req = new EMSJSONObjectRequest(method, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String newPRID = response.getString("Push_Registration_Id");
                        setPRIDAndFirePRIDCallback(newPRID);
                        if (pridCallback != null) {
                            pridCallback.onPRIDReceived(newPRID);
                        }
                    } catch (JSONException ex) {
                        Log.d(TAG, "Unable to find prid in response from registration: " + response.toString());
                        Log.d(TAG, "JSONException: " + ex.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Unable to request prid");
                }
            });
            VolleySender.getInstance(context).addToRequestQueue(req);
        } else {
            //Force this callback so the developer knows that the prid is available
            if (pridCallback != null) {
                pridCallback.onPRIDReceived(getPRID());
            }
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(CDMS_TOKEN, token);
        editor.apply();
        Log.d(TAG,"Stored DeviceID: " + token);
        this.token = token;
    }

     /**
     * Returns the CCMP Application ID used to initialize the EMS SDK
     * @param callingContext the originating android.content.Context object which made this call.
     * @return Returns the CCMP Application ID used to initialize the EMS SDK
     */
    public String getAppID(Context callingContext) {
        return GetPrivateSharedPreferences(callingContext).getString(CDMS_APPID, null);
    }

    /**
     * Returns the CCMP Application ID used to initialize the EMS SDK
     * @return Returns the CCMP Application ID used to initialize the EMS SDK
     */
    public String getAppID() {
        return getAppID(this.context);
    }

    private void setAppID(String appID) {
        SharedPreferences.Editor editor = GetPrivateSharedPreferences().edit();
        editor.putString(CDMS_APPID, appID);
        editor.apply();
        this.appID = appID;
    }

    /**
     * Returns the CCMP Customer ID used to initialize the EMS SDK
     * @param callingContext the originating android.content.Context object which made this call.
     * @return Returns the CCMP Customer ID used to initialize the EMS SDK
     */
    public int getCustomerID(Context callingContext) { return GetPrivateSharedPreferences(callingContext).getInt(CDMS_CUSTID, 0); }

    /**
     * Returns the CCMP Customer ID used to initialize the EMS SDK
     * @return Returns the CCMP Customer ID used to initialize the EMS SDK
     */
    public int getCustomerID() { return getCustomerID(this.context); }

    private void setCustomerID(int customerID) {
        SharedPreferences sharedPref = GetPrivateSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CDMS_CUSTID, customerID);
        editor.apply();
        this.customerID = customerID;
    }

    /**
     * Returns the CCMP region used to initialize the EMS SDK
     * @param callingContext the originating android.content.Context object which made this call.
     * @return Returns the CCMP region used to initialize the EMS SDK
     */
    public Region getRegion(Context callingContext) { return Region.values()[GetPrivateSharedPreferences(callingContext).getInt(CDMS_REGION, 0)]; }

    /**
     * Returns the CCMP region used to initialize the EMS SDK
     * @return Returns the CCMP region used to initialize the EMS SDK
     */
    public Region getRegion() { return getRegion(this.context); }

    private void setRegion(Region region) {
        SharedPreferences sharedPref = GetPrivateSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CDMS_REGION, region.getValue());
        editor.apply();
        this.region = region;
    }

    /**
     * Returns the Version Code of the EMSMobile SDK library
     * @return version code
     */
    public int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * Returns the Version Name of the EMSMobile SDK library
     * @return version name
     */
    public String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    private EMSMobileSDK() {
    }

    /**
     * Initialization of the SDK.  This method should be called before any other calls to the EMS SDK.
     *
     * @param ctx        the application context
     * @param appID      the CCMP application ID for the application
     * @param customerID the CCMP customer ID for the application
     */
    public void init(Context ctx, String appID, int customerID) {
        this.init(ctx, this.appID, this.customerID, region.NORTH_AMERICA);
    }

    /**
     * Initialization of the SDK.  This method should be called before any other calls to the EMS SDK.
     *
     * @param ctx        the application context
     * @param appID      the CCMP application ID for the application
     * @param customerID the CCMP customer ID for the application
     * @param region     the region for your instance of CCMP
     */
    public void init(Context ctx, String appID, int customerID, Region region) {
        this.context = ctx;
        setAppID(appID);
        setCustomerID(customerID);
        setRegion(region);
    }

     /**
     * Initialization of the SDK from calling context.  This method assumes previous setting/saving of
     * appID, customerID, and region parameters via a call to the longer-form init()
     *
     * @param ctx        the application context
     */
     public void initFromContext(Context ctx) throws Exception {

        String errorSuffix = " has not been set via init().";
        if ( ! GetPrivateSharedPreferences(ctx).contains(CDMS_APPID) ) { throw new InvalidParameterException(TAG + "Application Id" + errorSuffix); }
        if ( ! GetPrivateSharedPreferences(ctx).contains(CDMS_CUSTID) ) { throw new InvalidParameterException(TAG + "Cust Id" + errorSuffix); }
        if ( ! GetPrivateSharedPreferences(ctx).contains(CDMS_REGION) ) { throw new InvalidParameterException(TAG + "Region" + errorSuffix); }
        this.context = ctx;
    }

    /**
     * This method should be called whenever the application is brought back
     * into the foreground.  Typically via the onResume() method within
     * MainActivity.
     */
    public void NotificationOptInStatusCheck()
    {
        /**
         * Matrix of possibilities:
         * Notifications Disabled + PRID Null = no-op
         * Notifications Enabled + PRID Null = register
         * Notifications Disabled + PRID Not Null = unregister
         * Notifications Disabled + PRID NULL = no-op
         * Notifications Enabled + PRID Not Null = no-op
         */

        boolean pridIsAssigned = getPRID() != null;
        boolean notificationsEnabled = NotificationsEnabled();
        // Notifications are enabled at the OS-level and this instance doesn't
        // yet have a PRID assigned remotely - opt in.
        if (notificationsEnabled && pridIsAssigned == false)
        {
            Log.i(TAG, "Opting device into notifications via Marketing Suite");
            SaveRemoteTokenAndSetPRID(pridIsAssigned);
        }
        // Notifications are disabled at the OS-level, but we have a PRID
        // assigned remotely - opt out.
        else if (notificationsEnabled == false && pridIsAssigned == true)
        {
            Log.i(TAG, "Opting device out of notifications via Marketing Suite");
            DeactivateRemoteToken();
        }
    }

    /**
     * Indicates whether Notifications are enabled for the instance
     *
     */
    public boolean NotificationsEnabled()
    {
        return NotificationManagerCompat.from(this.context).areNotificationsEnabled();
    }


    /**
     * Registers a callbck to be called for any new prid received by CCMP
     *
     * @param pridCallback
     */
    public void RegisterPRIDCallback(IEMSPRIDCallback pridCallback) {
        this.pridCallback = pridCallback;
    }

    /**
     * This method is called to process the ems_open url either by the SDK NotificationReciever or
     * can be called directly from an overriden receiver in app code.
     *
     * @param ctx
     * @param intent
     */
    public void pushNotificationRegisterOpen(Context ctx, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //If the ems_open property is set, CCMP is notified that the application was opened by way of a Notification Tap.
            String ems_open = extras.getString("ems_open");
            if (ems_open != null) {
                Log.d(TAG, "App Open URL: " + ems_open);
                EMSStringRequest req = new EMSStringRequest(Request.Method.GET, ems_open, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "App Open Sent");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error calling ems_open url: " + error.getMessage());
                    }

                });
                VolleySender.getInstance(ctx).addToRequestQueue(req);
            }
        }
    }

    /**
     * Performs API Post and notifies callback
     * @param formId id of form to post
     * @param data Map of key/value pairs of data to send to form
     * @param callback function to be called when post is received
     */
    public void apiPost(final int formId, final Map<String, String> data, final IEMSAPIPostCallback callback) {
        String url = this.region.getAPIEndpoint() + "/post.aspx";
        EMSStringRequest req = new EMSStringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (callback != null)
                    callback.onDataSent(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (callback != null)
                    callback.onDataSent(error);
            }
        }){
            @Override
            public byte[] getBody() {
                StringBuilder sb = new StringBuilder();
                sb.append("cr=" + EMSMobileSDK.Default().getCustomerID() + "&fm=" + formId);
                for (String key : data.keySet()) {
                    if (data.get(key) != null)
                        sb.append("&" + key + "=" + data.get(key).toString());
                }
                return sb.toString().getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        VolleySender.getInstance(this.context).addToRequestQueue(req);
    }
}
