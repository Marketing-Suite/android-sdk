package experian.mobilesdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EMSMobileSDKTest {
    private final String APP_ID = "0d941093-9f85-41d3-a92b-0cf205183a51";
    private final String TOKEN1 = "fe5da804bb6167fa8a1fe44164828d5bfd853521ebc93f683de7bc4edf9a360d";
    private final String TOKEN2 = "1234567890987654321";
    private final String PRID = "abcdefghijklmnopqrstuvwxyz";
    private final String DEFAULT_URL = "https://www.google.com";
    private final String ERROR_NO_APPID = EMSMobileSDK.TAG + " Application Id has not been set via init().";
    private final String ERROR_NO_CUSTID = EMSMobileSDK.TAG + " Cust Id has not been set via init().";
    private final String ERROR_NO_REGION = EMSMobileSDK.TAG + " Region has not been set via init().";
    private final String ERROR_INVALID_CUSTID = "customerID cannot be 0";
    private final int CUSTOMER_ID = 100;
    private final Region REGION = Region.NORTH_AMERICA;
    private Context context = RuntimeEnvironment.application;
    private static boolean initialize;

    @Before
    public void setup() {
        if (initialize) {
            EMSMobileSDK.Default().init(context, APP_ID, CUSTOMER_ID, REGION);
        }
    }

    @Test
    public void aTestInitFromContext_noAppId() {
        try {
            EMSMobileSDK.Default().initFromContext(context);
        } catch (Exception e) {
            Assert.assertEquals(ERROR_NO_APPID, e.getMessage());
        }
    }

    @Test
    public void aTestInitFromContext_noCustomerId() {
        context.getSharedPreferences(EMSMobileSDK.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putString(EMSMobileSDK.CDMS_APPID, APP_ID).apply();
        try {
            EMSMobileSDK.Default().initFromContext(context);
        } catch (Exception e) {
            Assert.assertEquals(ERROR_NO_CUSTID, e.getMessage());
        }
    }

    @Test
    public void aTestInitFromContext_noRegion() {
        context.getSharedPreferences(EMSMobileSDK.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putString(EMSMobileSDK.CDMS_APPID, APP_ID).apply();
        context.getSharedPreferences(EMSMobileSDK.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putInt(EMSMobileSDK.CDMS_CUSTID, CUSTOMER_ID).apply();
        try {
            EMSMobileSDK.Default().initFromContext(context);
        } catch (Exception e) {
            Assert.assertEquals(ERROR_NO_REGION, e.getMessage());
        }
    }

    @Test
    public void aTestInit_invalidCustomerId() {
        try{
            EMSMobileSDK.Default().init(context, APP_ID, 0);
        } catch (Exception e) {
            Assert.assertEquals(ERROR_INVALID_CUSTID, e.getMessage());
        }
        // Set initialize to true since test cases for bad initialization are finished
        initialize = true;
    }

    @Test
    public void testInit_withoutRegion() {
        EMSMobileSDK.Default().init(context, APP_ID, CUSTOMER_ID);
        Assert.assertEquals(Region.NORTH_AMERICA, EMSMobileSDK.Default().getRegion());
        Assert.assertEquals(Region.NORTH_AMERICA, EMSMobileSDK.Default().getRegion(context));
    }

    @Test
    public void testGetAppID_successfulInitialization() {
        Assert.assertEquals(APP_ID, EMSMobileSDK.Default().getAppID());
        Assert.assertEquals(APP_ID, EMSMobileSDK.Default().getAppID(context));
    }

    @Test
    public void testGetCustomerId_successfulInitialization() {
        Assert.assertEquals(CUSTOMER_ID, EMSMobileSDK.Default().getCustomerID());
        Assert.assertEquals(CUSTOMER_ID, EMSMobileSDK.Default().getCustomerID(context));
    }

    @Test
    public void testGetRegion_successfulInitialization() {
        Assert.assertEquals(REGION, EMSMobileSDK.Default().getRegion());
    }

    @Test
    public void testSetDeviceToken_token() {

        EMSMobileSDK.Default().setToken(TOKEN2);
        EMSMobileSDK.Default().setToken(TOKEN2);
        Assert.assertEquals(TOKEN2, EMSMobileSDK.Default().getToken());
        EMSMobileSDK.Default().setToken(context, TOKEN1);
        Assert.assertEquals(TOKEN1, EMSMobileSDK.Default().getToken());

        // Add callback to trigger onPRIDReceived since token is already stored
        EMSMobileSDK.Default().registerPRIDCallback(new IEMSPRIDCallback() {
            @Override
            public void onPRIDReceived(String PRID) {
                // Should be null since we did not store any PRID
                Assert.assertNull(PRID);
                // Remove callback for other test cases
                EMSMobileSDK.Default().registerPRIDCallback(null);
            }
        });
        // Set Token again for use case of trying to save same token
        EMSMobileSDK.Default().setToken(context, TOKEN1);
    }

    @Test
    public void testHandleDeeplink_deepLinkUrl() {
        Intent intent = new Intent();
        intent.setData(Uri.parse(DEFAULT_URL));
        StringRequest deepLink = EMSMobileSDK.Default().handleDeepLink(intent);
        deepLink.deliverError(new NoConnectionError());
        Assert.assertNotNull(deepLink);
    }

    @Test
    public void testApiPost_error() {
        VolleyError volleyError = new VolleyError();
        EMSStringRequest stringRequest = EMSMobileSDK.Default().apiPost(1, new HashMap<>(), new IEMSAPIPostCallback() {
            @Override
            public void onDataSent(VolleyError error) {
                Assert.assertEquals(volleyError, error);
            }
        });
        stringRequest.deliverError(volleyError);
    }

    @Test
    public void testApiPost_success() {
        EMSStringRequest stringRequest = EMSMobileSDK.Default().apiPost(1, new HashMap<>(), new IEMSAPIPostCallback() {
            @Override
            public void onDataSent(VolleyError error) {
                Assert.assertNull(error);
            }
        });
        stringRequest.deliverResponseAsString("");
    }

    @Test
    public void testApiPost_body() throws AuthFailureError {
        VolleyError volleyError = new VolleyError();
        HashMap<String, String> map = new HashMap<>();
        map.put("TESTKEY", "TESTVALUE");
        EMSStringRequest stringRequest = EMSMobileSDK.Default().apiPost(1, map, new IEMSAPIPostCallback() {
            @Override
            public void onDataSent(VolleyError error) {
                Assert.assertEquals(volleyError, error);
            }
        });
        stringRequest.deliverError(volleyError);
        String result = new String(stringRequest.getBody());
        Assert.assertThat(result, CoreMatchers.containsString("TESTKEY"));
        Assert.assertThat(result, CoreMatchers.containsString("TESTVALUE"));
    }

    @Test
    public void testpushNotificationRegisterOpen_hasIntentExtra() {
        Intent intent = new Intent();
        intent.putExtra(EMSMobileSDK.EXTRA_EMS, DEFAULT_URL);
        EMSStringRequest emsStringRequest = EMSMobileSDK.Default().pushNotificationRegisterOpen(context, intent);
        emsStringRequest.deliverResponseAsString("");
        emsStringRequest.deliverError(new VolleyError());
        Assert.assertNotNull(emsStringRequest);
    }

    @Test
    public void testpushNotificationRegisterOpen_noIntentExtra() {
        Intent intent = new Intent();
        EMSStringRequest emsStringRequest = EMSMobileSDK.Default().pushNotificationRegisterOpen(context, intent);
        Assert.assertNull(emsStringRequest);
    }

    @Test
    public void testRegisterPRIDCallback_pRIDCallback() {
        IEMSPRIDCallback iemspridCallback = new IEMSPRIDCallback() {
            @Override
            public void onPRIDReceived(String PRID) {
                Assert.assertNotNull(PRID);
            }
        };
        EMSMobileSDK.Default().registerPRIDCallback(iemspridCallback);
        Assert.assertEquals(iemspridCallback, EMSMobileSDK.Default().getPRIDCallback());
        EMSMobileSDK.Default().getPRIDCallback().onPRIDReceived("");
    }

    @Test
    public void testGetVersionName_versionCode() {
        Assert.assertEquals(BuildConfig.VERSION_CODE, EMSMobileSDK.Default().getVersionCode());
    }

    @Test
    public void testGetVersionName_versionName() {
        Assert.assertEquals(BuildConfig.VERSION_NAME, EMSMobileSDK.Default().getVersionName());
    }

    @Test
    public void testNotificationOptInStatusCheck_ignoreSavingAndDeactivation() {
        EMSMobileSDK emsMobileSDK = PowerMockito.spy(EMSMobileSDK.Default());
        PowerMockito.when(emsMobileSDK.isNotificationEnabled()).thenReturn(false);
        Request request = emsMobileSDK.notificationOptInStatusCheck();
        Assert.assertNull(request);
    }

    @Test
    public void testNotificationOptInStatusCheck_saveRemoteToken() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Push_Registration_Id", PRID);
        Request request = EMSMobileSDK.Default().notificationOptInStatusCheck();
        request.deliverError(new VolleyError());
        if (request instanceof EMSJSONObjectRequest) {
            ((EMSJSONObjectRequest) request).deliverResponseAsJSON(jsonObject);
        }
    }

    @Test
    public void testNotificationOptInStatusCheck_deactivateRemoteToken() throws JSONException {
        // Save PRID first
        EMSJSONObjectRequest emsjsonObjectRequest = EMSMobileSDK.Default().saveRemoteTokenAndSetPRID(true);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Push_Registration_Id", PRID);
        // Trigger this to execute saving of PRID
        emsjsonObjectRequest.deliverResponseAsJSON(jsonObject);
        emsjsonObjectRequest.deliverError(new VolleyError());

        // Execute notificationOptInStatusCheck
        EMSMobileSDK emsMobileSDK = PowerMockito.spy(EMSMobileSDK.Default());
        PowerMockito.when(emsMobileSDK.isNotificationEnabled()).thenReturn(false);
        Request request = emsMobileSDK.notificationOptInStatusCheck();
        request.deliverError(new VolleyError());
        if (request instanceof EMSStringRequest) {
            ((EMSStringRequest) request).deliverResponseAsString("");
        }
        Assert.assertNull(EMSMobileSDK.Default().getPRID());
    }

    @Test
    public void testSaveRemoteTokenAndSetPRID() throws JSONException {
        IEMSPRIDCallback iemspridCallback = new IEMSPRIDCallback() {
            @Override
            public void onPRIDReceived(String id) {
                Assert.assertEquals(PRID, id);
            }
        };
        EMSMobileSDK.Default().registerPRIDCallback(iemspridCallback);

        EMSJSONObjectRequest emsjsonObjectRequest = EMSMobileSDK.Default().saveRemoteTokenAndSetPRID(true);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Push_Registration_Id", PRID);
        // Trigger this to execute saving of PRID
        emsjsonObjectRequest.deliverResponseAsJSON(jsonObject);
        emsjsonObjectRequest.deliverResponseAsJSON(jsonObject);
        emsjsonObjectRequest.deliverError(new VolleyError());
        Assert.assertEquals(PRID, EMSMobileSDK.Default().getPRID());
    }

    @Test
    public void testSaveRemoteTokenAndSetPRID_sameToken() throws JSONException {
        IEMSPRIDCallback iemspridCallback = new IEMSPRIDCallback() {
            @Override
            public void onPRIDReceived(String id) {
                Assert.assertEquals(PRID, id);
            }
        };
        EMSMobileSDK.Default().registerPRIDCallback(iemspridCallback);

        EMSJSONObjectRequest emsjsonObjectRequest = EMSMobileSDK.Default().saveRemoteTokenAndSetPRID(true);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Push_Registration_Id", PRID);
        // Trigger this to execute saving of PRID
        emsjsonObjectRequest.deliverResponseAsJSON(jsonObject);
        emsjsonObjectRequest.deliverError(new VolleyError());
        Assert.assertEquals(PRID, EMSMobileSDK.Default().getPRID());
    }

    @Test
    public void testSaveRemoteTokenAndSetPRID_jsonException() throws JSONException {
        EMSMobileSDK.Default().registerPRIDCallback(null);

        EMSJSONObjectRequest emsjsonObjectRequest = EMSMobileSDK.Default().saveRemoteTokenAndSetPRID(true);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aa", 10);
        // Trigger this to execute saving of PRID
        emsjsonObjectRequest.deliverResponseAsJSON(jsonObject);
        emsjsonObjectRequest.deliverError(new VolleyError());
        Assert.assertNull(EMSMobileSDK.Default().getPRID());
    }

    @Test
    public void testDeactivateRemoteToken() {
        EMSStringRequest stringRequest = EMSMobileSDK.Default().deactivateRemoteToken();
        stringRequest.deliverResponseAsString("");
        stringRequest.deliverError(new VolleyError());
        Assert.assertNull(EMSMobileSDK.Default().getPRID());
        Assert.assertNull(EMSMobileSDK.Default().getPRID(context));
    }
}
