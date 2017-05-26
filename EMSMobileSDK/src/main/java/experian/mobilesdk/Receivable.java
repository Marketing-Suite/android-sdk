package experian.mobilesdk;

import org.json.JSONObject;

/**
 * This creates an interface to make a simple observer pattern from messages when they are received.
 */
interface Receivable {
    public void onReceive(EMSMessage msg, int responseCode, String ResponseData);
}
