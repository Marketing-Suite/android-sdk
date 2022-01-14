package experian.mobilesdk;

import android.content.Context;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class EMSStringRequestTest {
    private final String APP_ID = "0d941093-9f85-41d3-a92b-0cf205183a51";
    private final String DEFAULT_URL = "https://google.com";
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
        EMSStringRequest emsStringRequest = new EMSStringRequest(Request.Method.GET, DEFAULT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Assert.assertNull(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Assert.assertEquals(noConnectionError, error);
            }
        });

        VolleySender.getInstance(context).addToRequestQueue(emsStringRequest);
        emsStringRequest.deliverError(noConnectionError);
    }

    @Test
    public void testDeliverError_noConnectionErrorMaxRetry() {
        NoConnectionError noConnectionError = new NoConnectionError();
        EMSStringRequest emsStringRequest = new EMSStringRequest(Request.Method.GET, DEFAULT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Assert.assertNull(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Assert.assertEquals(noConnectionError, error);
            }
        });
        emsStringRequest.setTag(3);
        VolleySender.getInstance(context).addToRequestQueue(emsStringRequest);
        emsStringRequest.deliverError(noConnectionError);

    }
}