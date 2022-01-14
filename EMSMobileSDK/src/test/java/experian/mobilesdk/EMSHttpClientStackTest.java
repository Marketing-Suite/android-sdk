package experian.mobilesdk;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class EMSHttpClientStackTest {
    EMSHttpClientStack emsHttpClientStack;
    HttpClient mockClient;
    HttpResponse mockResponse;

    @Before
    public void setUp() {
        mockClient = Mockito.mock(HttpClient.class);
        mockResponse = Mockito.mock(HttpResponse.class);
        emsHttpClientStack = new EMSHttpClientStack(mockClient);
    }

    @Test
    public void testPerformRequest_DeprecatedGetOrPost() {
        HttpResponse response = performRequest(Request.Method.DEPRECATED_GET_OR_POST, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_DeprecatedGetOrPostWithBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpResponse response = performRequest(Request.Method.DEPRECATED_GET_OR_POST, jsonObject.toString());
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_Get() {
        HttpResponse response = performRequest(Request.Method.GET, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_Delete() {
        HttpResponse response = performRequest(Request.Method.DELETE, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_Post() {
        HttpResponse response = performRequest(Request.Method.POST, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_Put() {
        HttpResponse response = performRequest(Request.Method.PUT, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_Head() {
        HttpResponse response = performRequest(Request.Method.HEAD, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_Options() {
        HttpResponse response = performRequest(Request.Method.OPTIONS, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_Trace() {
        HttpResponse response = performRequest(Request.Method.TRACE, null);
        Assert.assertEquals(mockResponse, response);
    }

    @Test
    public void testPerformRequest_UnknownRequestMethod() {
        try {
            HttpResponse response = performRequest(100, null);
            Assert.assertEquals(mockResponse, response);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testPerformRequest_Patch() {
        performRequest(Request.Method.PATCH, null);
    }

    private HttpResponse performRequest(int method, String requestBody) {
        Request request = new Request(method, Region.NORTH_AMERICA.getAPIEndpoint(), errorListener) {
            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                return null;
            }

            @Override
            protected void deliverResponse(Object response) {

            }

            @Override
            public byte[] getPostBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        try {
            Mockito.when(mockClient.execute(Mockito.any())).thenReturn(mockResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return emsHttpClientStack.performRequest(request, new HashMap<>());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        return null;
    }

    private Response.ErrorListener errorListener = error -> {

    };
}