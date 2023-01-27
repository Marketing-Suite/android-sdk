package experian.mobilesdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import java.security.InvalidParameterException;
import java.util.Map;

import experian.mobilesdk.model.ApiResponse;
import experian.mobilesdk.model.DeviceToken;
import experian.mobilesdk.model.TokenResponse;
import experian.mobilesdk.network.MessagingAPI;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * This is the main entry point for the SDK that is exposed to the User It contains the
 * initialization and the hooks for registering call back events.
 */
public class EMSMobileSDK {
  // Static keys for SharedPreferences
  public static final String TAG = "EMS:EMSMobileSDK";
  public static final String SHARED_PREFERENCES_NAME = "EMSMobileSDK";
  public static final String CDMS_PRID = "CDMS_PRID";
  public static final String CDMS_CUSTID = "CDMS_CUSTID";
  public static final String CDMS_APPID = "CDMS_APPID";
  public static final String CDMS_TOKEN = "CDMS_TOKEN";
  public static final String CDMS_REGION = "CDMS_REGION";
  public static final String EXTRA_EMS = "ems_open";
  private static EMSMobileSDK mInstance = new EMSMobileSDK();
  private Context mContext;
  private String mAppId;
  private int mCustomerId;
  private String mToken;
  private Region mRegion;
  private String mPRID;

  private MessagingAPI messagingAPI;

  // Callback interfaces
  private IEMSPRIDCallback mPRIDCallback;

  private EMSMobileSDK() {}

  public static EMSMobileSDK Default() {
    return mInstance;
  }

  /**
   * Initialization of the SDK. This method should be called before any other calls to the EMS SDK.
   *
   * @param ctx the application context
   * @param appID the CCMP application ID for the application
   * @param customerID the CCMP customer ID for the application
   */
  public void init(Context ctx, String appID, int customerID) {
    this.init(ctx, appID, customerID, mRegion.NORTH_AMERICA);
  }

  /**
   * Initialization of the SDK. This method should be called before any other calls to the EMS SDK.
   *
   * @param ctx the application context
   * @param appID the CCMP application ID for the application
   * @param customerID the CCMP customer ID for the application
   * @param region the region for your instance of CCMP
   */
  public void init(Context ctx, String appID, int customerID, Region region) {
    this.mContext = ctx;
    setAppID(appID);
    setCustomerID(customerID);
    setRegion(region);
    messagingAPI = new MessagingAPI(getEndpoint());
  }

  /**
   * Initialization of the SDK from calling context. This method assumes previous setting/saving of
   * appID, customerID, and region parameters via a call to the longer-form init()
   *
   * @param ctx the application context
   */
  public void initFromContext(Context ctx) throws Exception {
    this.mContext = ctx;
    String errorSuffix = " has not been set via init().";
    if (!getPrivateSharedPreferences(ctx).contains(CDMS_APPID)) {
      throw new InvalidParameterException(TAG + " Application Id" + errorSuffix);
    }
    if (!getPrivateSharedPreferences(ctx).contains(CDMS_CUSTID)) {
      throw new InvalidParameterException(TAG + " Cust Id" + errorSuffix);
    }
    if (!getPrivateSharedPreferences(ctx).contains(CDMS_REGION)) {
      throw new InvalidParameterException(TAG + " Region" + errorSuffix);
    }

    if (messagingAPI == null) {
      messagingAPI = new MessagingAPI(getEndpoint());
    }
  }

  /**
   * Gets the instance of the Context
   *
   * @return context
   */
  public Context getContext() {
    return this.mContext;
  }

  /**
   * @param callingContext the originating android.content.Context object which made this call.
   * @return returns the private shared preferences for the SDK
   */
  private SharedPreferences getPrivateSharedPreferences(Context callingContext) {
    return callingContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
  }

  /**
   * @return returns the private shared preferences for the SDK
   */
  private SharedPreferences getPrivateSharedPreferences() {
    return getPrivateSharedPreferences(this.mContext);
  }

  /**
   * Returns the CCMP Application ID used to initialize the EMS SDK
   *
   * @param callingContext the originating android.content.Context object which made this call.
   * @return Returns the CCMP Application ID used to initialize the EMS SDK
   */
  public String getAppID(Context callingContext) {
    return getPrivateSharedPreferences(callingContext).getString(CDMS_APPID, null);
  }

  /**
   * Returns the CCMP Application ID used to initialize the EMS SDK
   *
   * @return Returns the CCMP Application ID used to initialize the EMS SDK
   */
  public String getAppID() {
    return getAppID(this.mContext);
  }

  private void setAppID(String appID) {
    SharedPreferences.Editor editor = getPrivateSharedPreferences().edit();
    editor.putString(CDMS_APPID, appID);
    editor.apply();
    this.mAppId = appID;
  }

  /**
   * Returns the CCMP Customer ID used to initialize the EMS SDK
   *
   * @param callingContext the originating android.content.Context object which made this call.
   * @return Returns the CCMP Customer ID used to initialize the EMS SDK
   */
  public int getCustomerID(Context callingContext) {
    return getPrivateSharedPreferences(callingContext).getInt(CDMS_CUSTID, 0);
  }

  /**
   * Returns the CCMP Customer ID used to initialize the EMS SDK
   *
   * @return Returns the CCMP Customer ID used to initialize the EMS SDK
   */
  public int getCustomerID() {
    return getCustomerID(this.mContext);
  }

  private void setCustomerID(int customerID) throws InvalidParameterException {
    if (customerID == 0) {
      throw new InvalidParameterException("customerID cannot be 0");
    }
    SharedPreferences sharedPref = getPrivateSharedPreferences();
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt(CDMS_CUSTID, customerID);
    editor.apply();
    this.mCustomerId = customerID;
  }

  /**
   * Returns the CCMP region used to initialize the EMS SDK
   *
   * @param callingContext the originating android.content.Context object which made this call.
   * @return Returns the CCMP region used to initialize the EMS SDK
   */
  public Region getRegion(Context callingContext) {
    return Region.values()[getPrivateSharedPreferences(callingContext).getInt(CDMS_REGION, 0)];
  }

  /**
   * Returns the CCMP region used to initialize the EMS SDK
   *
   * @return Returns the CCMP region used to initialize the EMS SDK
   */
  public Region getRegion() {
    return getRegion(this.mContext);
  }

  private void setRegion(Region region) {
    SharedPreferences sharedPref = getPrivateSharedPreferences();
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt(CDMS_REGION, region.getValue());
    editor.apply();
    this.mRegion = region;
  }

  /**
   * Builds the endpoint specific for this Customer and Application
   *
   * @return URL endpoint of the xts app
   */
  public String getEndpoint() {
    StringBuilder sbUrlBuilder = new StringBuilder();
    sbUrlBuilder
        .append(getRegion().getEndpoint())
        .append("/xts/registration/cust/")
        .append(getCustomerID())
        .append("/application/")
        .append(getAppID())
        .append("/");
    return sbUrlBuilder.toString();
  }

  /**
   * Returns the prid created by CCMP to identify this device
   *
   * @param callingContext the originating android.content.Context object which made this call.
   * @return returns the prid created by CCMP to identify this device
   */
  public String getPRID(Context callingContext) {
    return getPrivateSharedPreferences(callingContext).getString(CDMS_PRID, null);
  }

  /**
   * Returns the prid created by CCMP to identify this device
   *
   * @return returns the prid created by CCMP to identify this device
   */
  public String getPRID() {
    return getPRID(this.mContext);
  }

  /**
   * Set the PRID locally, and if a callback is set fire it. If the PRID is unchanged, this is
   * considered a no-op, and the callback will not be fired.
   *
   * @param prid new PRID to set in object instance
   */
  private void setPRIDAndFirePRIDCallback(String prid) {
    String originalPRID = getPRID();
    // no-op
    if (originalPRID != null && originalPRID.equals(prid)) {
      return;
    }

    SharedPreferences sharedPref =
        mContext.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(CDMS_PRID, prid);
    editor.apply();
    this.mPRID = prid;
    // Don't call the callback in the case that we're null'ing out the PRID
    if (prid != null && mPRIDCallback != null) {
      mPRIDCallback.onPRIDReceived(prid);
    }
  }

  /**
   * Returns the Device token assigned by Google Services
   *
   * @return returns the device token assigned by Google Services
   */
  public String getToken() {
    return getPrivateSharedPreferences().getString(CDMS_TOKEN, "");
  }

  /**
   * This method is called to set the Device token on Init or Refresh
   *
   * @param token The device token received from Google's Firebase
   */
  public void setToken(String token) {
    String originalToken = getToken();
    if (originalToken.equals(token)) {
      return;
    }
    // new or updated
    saveRemoteTokenAndSetPRID(getPRID() == null);

    // set the token locally
    SharedPreferences.Editor editor = getPrivateSharedPreferences().edit();
    editor.putString(CDMS_TOKEN, token);
    editor.apply();
    this.mToken = token;
  }

  /**
   * Store the device token in Marketing Suite, and depending on whether a PRID has been assigned
   * for this token, save the PRID locally
   *
   * @param pridIsAssigned boolean indicating whether a PRID has been assigned via Marketing Suite
   *     for the device token being saved
   * @return current request that is either queued or processed
   */
  public void saveRemoteTokenAndSetPRID(boolean pridIsAssigned) {
    if (!pridIsAssigned) {
      messagingAPI.registerToken(tokenSubmissionBody()).enqueue(getTokenRemoteResponseCallback());
    } else {
      messagingAPI
          .updateToken(getPRID(), tokenSubmissionBody())
          .enqueue(getTokenRemoteResponseCallback());
    }
  }

  /**
   * Mark the token remotely as having been deactivated, indicating push notifications are opted-out
   * for this device.
   *
   * @return current request that is either queued or processed
   */
  public void deactivateRemoteToken() {
    messagingAPI.deactivateToken(tokenSubmissionBody()).enqueue(getDeactivateTokenCallback());
  }

  private DeviceToken tokenSubmissionBody() {
    return new DeviceToken(getToken());
  }

  /**
   * After remote submission of a token, the response is returned as JSON, and parsed here. If the
   * PRID is new, call {@link #setPRIDAndFirePRIDCallback(String)}
   *
   * @param tokenResponse response from the server
   */
  private void setPRIDFromRemoteResponse(TokenResponse tokenResponse) {
    setPRIDAndFirePRIDCallback(tokenResponse.getPushRegistrationId());
  }

  /**
   * This method is called to set the Device token on Init or Refresh when application context is
   * required
   *
   * @param context
   * @param token
   */
  public void setToken(Context context, String token) {
    SharedPreferences sharedPref =
        context.getSharedPreferences("EMSMobileSDK", Context.MODE_PRIVATE);
    String storedToken = sharedPref.getString(CDMS_TOKEN, "");
    if (!storedToken.equals(token)) {
      String storedPRID = getPRID();
      // Register this token and get PRID
      DeviceToken tokenBody = new DeviceToken(token);
      if (storedPRID != null) {
        messagingAPI.updateToken(storedPRID, tokenBody).enqueue(getTokenResponseCallback());
      } else {
        messagingAPI.registerToken(tokenBody).enqueue(getTokenResponseCallback());
      }
    } else {
      // Force this callback so the developer knows that the prid is available
      if (mPRIDCallback != null) {
        mPRIDCallback.onPRIDReceived(getPRID());
      }
    }
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(CDMS_TOKEN, token);
    editor.apply();
    Log.d(TAG, "Stored DeviceID: " + token);
    this.mToken = token;
  }

  /**
   * This method should be called whenever the application is brought back into the foreground.
   * Typically via the onResume() method within MainActivity.
   *
   * @return current request that is either queued or processed
   */
  public void notificationOptInStatusCheck() {
    /**
     * Matrix of possibilities: Notifications Disabled + PRID Null = no-op Notifications Enabled +
     * PRID Null = register Notifications Disabled + PRID Not Null = unregister Notifications
     * Disabled + PRID NULL = no-op Notifications Enabled + PRID Not Null = no-op
     */
    boolean pridIsAssigned = getPRID() != null;
    boolean notificationsEnabled = isNotificationEnabled();
    // Notifications are enabled at the OS-level and this instance doesn't
    // yet have a PRID assigned remotely - opt in.
    if (notificationsEnabled && pridIsAssigned == false) {
      Log.i(TAG, "Opting device into notifications via Marketing Suite");
      saveRemoteTokenAndSetPRID(pridIsAssigned);
    }
    // Notifications are disabled at the OS-level, but we have a PRID
    // assigned remotely - opt out.
    else if (notificationsEnabled == false && pridIsAssigned == true) {
      Log.i(TAG, "Opting device out of notifications via Marketing Suite");
      deactivateRemoteToken();
    }
  }

  /** Indicates whether Notifications are enabled for the instance */
  public boolean isNotificationEnabled() {
    return NotificationManagerCompat.from(this.mContext).areNotificationsEnabled();
  }

  /**
   * Registers a callbck to be called for any new prid received by CCMP
   *
   * @param pridCallback
   */
  public void registerPRIDCallback(IEMSPRIDCallback pridCallback) {
    this.mPRIDCallback = pridCallback;
  }

  /**
   * @return callback registered, null if no callback is registered
   */
  public IEMSPRIDCallback getPRIDCallback() {
    return mPRIDCallback;
  }

  /**
   * This method is called to process the ems_open url either by the SDK NotificationReciever or can
   * be called directly from an overriden receiver in app code.
   *
   * @param ctx
   * @param intent
   */
  public void pushNotificationRegisterOpen(Context ctx, Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras != null) {
      // If the ems_open property is set, CCMP is notified that the application was opened by way of
      // a Notification Tap.
      String emsOpenUrl = extras.getString(EXTRA_EMS);
      if (emsOpenUrl != null) {
        Log.d(TAG, "App Open URL: " + emsOpenUrl);
        messagingAPI.trackEmsOpen(emsOpenUrl).enqueue(getEmsOpenCallback());
      }
    }
  }

  /**
   * Performs API Post and notifies callback
   *
   * @param formId id of form to post
   * @param data Map of key/value pairs of data to send to form
   * @param callback function to be called when post is received
   */
  public void apiPost(
      final int formId, final Map<String, Object> data, final IEMSAPIPostCallback callback) {
    String url = this.mRegion.getAPIEndpoint() + "/post.aspx";

    // Add customer and form information
    data.put("cr", EMSMobileSDK.Default().getCustomerID());
    data.put("fm", formId);

    messagingAPI.postApi(url, data).enqueue(getPostApiCallback(callback));
  }

  /**
   * The HandleDeepLink function parses the information from the userActivity and returns the
   * original Deep link URL, the Deep link Paramater if any, and finally register the link count on
   * CCMP.
   *
   * @param intent the intent-filter set up in android manifest to pass the deep link url values
   * @return original DeepLink URL
   */
  public void handleDeepLink(final Intent intent) {
    final EMSDeepLink deepLink = new EMSDeepLink(intent);
    messagingAPI
        .openDeepLink(deepLink.getDeepLinkUrl())
        .enqueue(getDeepLinkCallback(deepLink.getDeepLinkUrl()));
  }

  protected Callback<TokenResponse> getTokenResponseCallback() {
    return new Callback<>() {
      @Override
      public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
        String newPRID = response.body().getPushRegistrationId();
        if (TextUtils.isEmpty(newPRID)) {
          // Show error
          Log.d(TAG, "Unable to request PRID: " + response.body().getMessage());
        } else {
          setPRIDAndFirePRIDCallback(newPRID);
          if (mPRIDCallback != null) {
            mPRIDCallback.onPRIDReceived(newPRID);
          }
        }
      }

      @Override
      public void onFailure(Call<TokenResponse> call, Throwable t) {
        Log.d(TAG, "Unable to request PRID: " + t);
      }
    };
  }

  protected Callback<TokenResponse> getTokenRemoteResponseCallback() {
    return new Callback<>() {
      @Override
      public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
        String newPRID = response.body().getPushRegistrationId();
        if (!TextUtils.isEmpty(newPRID)) {
          setPRIDFromRemoteResponse(response.body());
        } else {
          Log.i(
              TAG, "Error submitting token registration request: " + response.body().getMessage());
        }
      }

      @Override
      public void onFailure(Call<TokenResponse> call, Throwable t) {
        Log.i(TAG, "Error submitting token registration request: " + t);
      }
    };
  }

  protected Callback<String> getDeactivateTokenCallback() {
    return new Callback<String>() {
      @Override
      public void onResponse(Call<String> call, retrofit2.Response<String> response) {
        Log.i(
            TAG,
            String.format("Received response upon deactivating token: %s", response.toString()));
        setPRIDAndFirePRIDCallback(null);
      }

      @Override
      public void onFailure(Call<String> call, Throwable t) {
        Log.i(
            TAG,
            String.format(
                "Error submitting registration request.  Error message: %s. Error toString(): %s",
                t.getMessage(), t.toString()));
      }
    };
  }

  protected Callback<Object> getEmsOpenCallback() {
    return new Callback() {

      @Override
      public void onResponse(Call call, retrofit2.Response response) {
        Log.d(TAG, "App Open Sent");
      }

      @Override
      public void onFailure(Call call, Throwable t) {
        Log.d(TAG, "Error calling ems_open url: " + t);
      }
    };
  }

  protected Callback<ApiResponse> getPostApiCallback(IEMSAPIPostCallback callback) {
    return new Callback<ApiResponse>() {

      @Override
      public void onResponse(Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {
        if (callback != null) callback.onDataSent(null);
      }

      @Override
      public void onFailure(Call<ApiResponse> call, Throwable t) {
        if (callback != null) callback.onDataSent(t.getMessage());
      }
    };
  }

  protected Callback<String> getDeepLinkCallback(String deeplinkUrl) {
    return new Callback<String>() {
      @Override
      public void onResponse(Call<String> call, retrofit2.Response<String> response) {
        Log.d(TAG, "Deep link url post successfully: " + deeplinkUrl);
      }

      @Override
      public void onFailure(Call<String> call, Throwable t) {
        Log.d(TAG, "Error processing deep link url: " + deeplinkUrl + ". See error message: " + t);
      }
    };
  }
}
