package experian.mobilesdk;

import android.util.Log;

/**
 * Created by Blaize Stewart on 1/11/2017.
 *
 * This class acts as a wrapper for logging.
 */

class AndroidLogger implements Loggable {
    @Override
    public void writeLog(String message) {
        Log.d("I", message);
    }

    @Override
    public void writeErrorLog(String message) {
        Log.d("E", message);
    }
}
