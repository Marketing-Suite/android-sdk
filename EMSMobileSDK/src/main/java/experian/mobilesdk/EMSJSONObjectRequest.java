package experian.mobilesdk;

import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.lang.reflect.Field;


/**
 * Class which creates new Request
 */
class EMSJSONObjectRequest extends JsonObjectRequest {
    private static final int TIMEOUT = 15000;
    private static final int MAX_RETRY = 3;

    /**
     * Constructor for EMSJSONObjectRequest
     *
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param jsonRequest   A JSONObject to post with the request. Null is allowed and indicates no parameters will be posted along with request.
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    EMSJSONObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.setTag(0);
    }

    /**
     * Delivers error message to the ErrorListener that the Request was initialized with.
     *
     * @param error error details
     */
    @Override
    public void deliverError(VolleyError error) {
        if (error instanceof NoConnectionError) {
            int attempts = (int) this.getTag();
            if (attempts == MAX_RETRY) {
                super.deliverError(error);
            } else {
                attempts++;
                this.setTag(attempts);
                try {
                    Field mRequestQueue = this.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("mRequestQueue");
                    mRequestQueue.setAccessible(true);
                    Thread.sleep((attempts * TIMEOUT) * attempts);
                    RequestQueue queue = (RequestQueue) mRequestQueue.get(this);
                    queue.add(this);
                } catch (NoSuchFieldException nofEx) {
                    super.deliverError(error);
                } catch (InterruptedException intEx) {
                    super.deliverError(error);
                } catch (IllegalAccessException ex) {
                    Log.d("TAG", "Unable to get queue for retry");
                }
            }
        } else {
            super.deliverError(error);
        }
    }
}
