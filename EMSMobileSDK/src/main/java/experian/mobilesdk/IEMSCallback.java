package experian.mobilesdk;

import org.json.JSONObject;

/**
 * Created by Blaize Stewart on 1/19/2017.
 *
 * A simple interface to enable Callbacks with string payloads.
 */

public interface IEMSCallback {
    public void Callback(String dataResponse);
}
