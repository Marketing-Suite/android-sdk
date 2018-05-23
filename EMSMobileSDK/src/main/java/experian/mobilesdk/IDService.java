package experian.mobilesdk;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * This is the Firebase ID Services that gets called when a Device Token changes.
 */

public class IDService extends FirebaseInstanceIdService {

    private static final String TAG = "IDService";

    @Override
    public void onTokenRefresh() {
        FirebaseInstanceIdService fid = new FirebaseInstanceIdService();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        try {
            EMSMobileSDK.Default().initFromContext(getApplicationContext());
        }
        catch (Exception e)
        {
            Log.e(TAG,"Error initializing EMSMObileSDK solely from application context. The SDK must first be initialized with customer mobile application settings");
        }
        EMSMobileSDK.Default().setToken(getApplicationContext(), refreshedToken);
    }
}