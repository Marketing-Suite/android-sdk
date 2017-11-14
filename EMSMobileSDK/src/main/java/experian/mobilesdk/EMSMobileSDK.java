package experian.mobilesdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import org.json.JSONException;
import org.json.JSONObject;
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
     * Returns the prid created by CCMP to identify this device
     * @return returns the prid created by CCMP to identify this device
     */
    public String getPRID() {
        if (this.prid == null) {
            SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
            this.prid = sharedPref.getString(CDMS_PRID, null);
            Log.d(TAG, "Retrieved prid from storage: " + this.prid);
        }
        return this.prid;
    }

    private void setPRID(String prid) {
        SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(CDMS_PRID, prid);
        editor.apply();
        this.prid = prid;
    }

    /**
     * Returns the Device token assigned by Google Services
     * @return returns the device token assigned by Google Services
     */
    public String getToken() {
        if (this.token == null) {
            SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
            this.token = sharedPref.getString(CDMS_TOKEN, null);
            Log.d(TAG, "Retrieved token from storage: " + this.token);
        }
        return this.token;
    }

    private void setToken(String token) {
        this.setToken(this.context, token);
    }

    /**
     * This method is called to set the Device token on Init or Refresh
     *
     * @param context
     * @param token
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
                        setPRID(newPRID);
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
     * @return Returns the CCMP Application ID used to initialize the EMS SDK
     */
    public String getAppID() {
        if (this.appID == null) {
            SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
            this.appID = sharedPref.getString(CDMS_APPID, null);
            Log.d(TAG, "Retrieved appID from storage: " + this.appID);
        }
        return this.appID;
    }

    private void setAppID(String appID) {
        SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(CDMS_APPID, appID);
        editor.apply();
        this.appID = appID;
    }

    /**
     * Returns the CCMP Customer ID used to initialize the EMS SDK
     * @return Returns the CCMP Customer ID used to initialize the EMS SDK
     */
    public int getCustomerID() {
        if (this.customerID == 0) {
            SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
            this.customerID = sharedPref.getInt(CDMS_CUSTID, 0);
            Log.d(TAG, "Retrieved customerID from storage: " + this.customerID);
        }
        return this.customerID;
    }

    private void setCustomerID(int customerID) {
        SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(CDMS_CUSTID, customerID);
        editor.apply();
        this.customerID = customerID;
    }

    /**
     * Returns the CCMP region used to initialize the EMS SDK
     * @return Returns the CCMP region used to initialize the EMS SDK
     */
    public Region getRegion() {
        if (this.region == null) {
            SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
            this.region = Region.values()[sharedPref.getInt(CDMS_REGION, 0)];
        }
        return this.region;
    }

    private void setRegion(Region region) {
        SharedPreferences sharedPref = context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
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
