package experian.mobilesdk;

import com.android.volley.VolleyError;

/**
 * Interface which handles API Post from the app to a form hosted in CCMP.
 */
public interface IEMSAPIPostCallback {
    /**
     * This method is called when the API Post is complete
     * If the error parameter passed to the method is null, the API Post was successful, otherwise the error will have the data about what exactly went wrong.
     * @param error
     */
    public void onDataSent(VolleyError error);
}
