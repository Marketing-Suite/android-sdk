package experian.mobilesdk;


import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * Created by Blaize Stewart on 1/13/2017.
 *
 * This class gives more access to the underlying message object from Volley so the HTTP status and response body are accesible.

 */

class ServerStatusRequestObject extends Request {


    private final Response.Listener mListener;
    private String mBody = "";
    private String mContentType;

    //The constructor takes the basic properties from Volley and creates a request object.
    public ServerStatusRequestObject(int method,
                                     String url,
                                     Response.Listener listener,
                                     Response.ErrorListener errorListener, Object body) {

        super(method, url, errorListener);
        mListener = listener;
        mContentType = "application/json";
        mBody = body.toString();


        if (method == Method.POST) {
            RetryPolicy policy = new DefaultRetryPolicy(5000, 0, 5);
            setRetryPolicy(policy);
        }
    }

    //Gives acces to the HTTP response object from Volley.
    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {

        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }


   @Override
    protected void deliverResponse(Object response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mBody.getBytes();
    }

    @Override
    public String getBodyContentType() {
        return mContentType;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }


}