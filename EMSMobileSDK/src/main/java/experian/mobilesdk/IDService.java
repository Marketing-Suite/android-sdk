package experian.mobilesdk;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Blaize Stewart on 1/10/2017.
 *
 * This is the Firebase ID Services that gets called when a Device Token changes.
 */

public class IDService extends FirebaseInstanceIdService {

    private static final String TAG = "IDService";

    @Override
    public void onTokenRefresh() {

        FirebaseInstanceIdService fid = new FirebaseInstanceIdService();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        EMSMobileSDK.Default().setDeviceToken(refreshedToken);
    }


}