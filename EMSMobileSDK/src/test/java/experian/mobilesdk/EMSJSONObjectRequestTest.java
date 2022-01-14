package experian.mobilesdk;

import android.content.Context;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=21)
public class EMSJSONObjectRequestTest {
    private final String APP_ID = "0d941093-9f85-41d3-a92b-0cf205183a51";
    private final int CUSTOMER_ID = 100;
    private final Region REGION = Region.NORTH_AMERICA;
    private Context context = RuntimeEnvironment.application;

    @Before
    public void setup() {
        EMSMobileSDK.Default().init(context, APP_ID, CUSTOMER_ID, REGION);
    }

    @Test
    public void testDeliverError_noConnectionError() {
        NoConnectionError noConnectionError = new NoConnectionError();
        EMSJSONObjectRequest emsjsonObjectRequest = new EMSJSONObjectRequest(Request.Method.POST,
                registrationEndpoint(Request.Method.POST), tokenSubmissionJsonBody(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Assert.assertNull(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Assert.assertEquals(noConnectionError, error);
                    }
                });
        VolleySender.getInstance(context).addToRequestQueue(emsjsonObjectRequest);
        emsjsonObjectRequest.deliverError(noConnectionError);
    }

    @Test
    public void testDeliverError_noConnectionErrorMaxRetry() {
        NoConnectionError noConnectionError = new NoConnectionError();
        EMSJSONObjectRequest emsjsonObjectRequest = new EMSJSONObjectRequest(Request.Method.POST,
                registrationEndpoint(Request.Method.POST), tokenSubmissionJsonBody(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Assert.assertNull(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Assert.assertEquals(noConnectionError, error);
                    }
                });
        emsjsonObjectRequest.setTag(3); // Current Max Retry as of 01-23-2020
        VolleySender.getInstance(context).addToRequestQueue(emsjsonObjectRequest);
        emsjsonObjectRequest.deliverError(noConnectionError);
    }

    private String registrationEndpoint(int method) {

        StringBuilder sbUrlBuilder = new StringBuilder();
        sbUrlBuilder.append(EMSMobileSDK.Default().getRegion().getEndpoint())
                .append("/xts/registration/cust/")
                .append(EMSMobileSDK.Default().getCustomerID())
                .append("/application/")
                .append(EMSMobileSDK.Default().getAppID());

        if (method == Request.Method.POST || method == Request.Method.DELETE)
            return sbUrlBuilder.append("/token").toString(); //baseUrl + "/token";
        else // PUT
            return sbUrlBuilder.append("/registration/")  //baseUrl + "/registration/" + getPRID() + "/token";
                    .append(EMSMobileSDK.Default().getPRID())
                    .append("/token")
                    .toString();
    }

    private JSONObject tokenSubmissionJsonBody() {
        JSONObject body = new JSONObject();
        try {
            body.put("DeviceToken", EMSMobileSDK.Default().getToken());
        } catch (JSONException ex) {
        }
        return body;
    }
}
