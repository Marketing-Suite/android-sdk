package experian.mobilesdk;

/**
 * Created by Blaize Stewart on 1/10/2017.
 *
 * Simple interface tor wrappers around logging.
 */

interface Loggable {
    void writeLog(String message);
    void writeErrorLog(String message);
}
