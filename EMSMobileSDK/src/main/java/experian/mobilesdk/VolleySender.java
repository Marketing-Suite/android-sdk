package experian.mobilesdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.HttpStack;

/**
 * This class is a wrapper  around Volley to handle the HTTP calls for the Queue.
 * https://developer.android.com/training/volley/index.html
 */

class VolleySender {
    private static VolleySender mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    /**
     * Constructor for VolleySender
     *
     * @param context of the application
     */
    private VolleySender(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    /**
     * Get the instance of the VolleySender
     *
     * @param context of the application
     * @return instance of {@link VolleySender}
     */
    public static synchronized VolleySender getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySender(context);
        }
        return mInstance;
    }

    /**
     * Retrieves the {@link RequestQueue}
     *
     * @return {@link RequestQueue}
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {

            String userAgent = "volley/0";
            try {
                String packageName = mCtx.getPackageName();
                PackageInfo info = mCtx.getPackageManager().getPackageInfo(packageName, 0);
                userAgent = packageName + "/" + info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
            }
            HttpStack httpStack = new EMSHttpClientStack(AndroidHttpClient.newInstance(userAgent));
            mRequestQueue = Volley.newRequestQueue(mCtx, httpStack);
        }
        return mRequestQueue;
    }

    /**
     * Add request to the {@link RequestQueue}
     *
     * @param req {@link Request}
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
