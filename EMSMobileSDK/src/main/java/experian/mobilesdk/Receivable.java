package experian.mobilesdk;

import org.json.JSONObject;

/**
 * Created by Blaize Stewart on 1/13/2017.
 *
 * This creates an interface to make a simple observer pattern from messages when they are received.
 */

interface Receivable {
    public void onReceive(EMSMessage msg, int responseCode, JSONObject ResponseData);
}
