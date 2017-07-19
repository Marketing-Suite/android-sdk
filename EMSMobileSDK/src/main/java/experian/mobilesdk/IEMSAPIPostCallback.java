package experian.mobilesdk;

import com.android.volley.VolleyError;

/**
 * Created by C17045A on 7/10/2017.
 */

public interface IEMSAPIPostCallback {
    public void onDataSent(VolleyError error);
}
