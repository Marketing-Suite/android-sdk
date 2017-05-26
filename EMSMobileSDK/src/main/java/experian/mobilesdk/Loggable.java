package experian.mobilesdk;

/**
 * Simple interface tor wrappers around logging.
 */

interface Loggable {
    void writeLog(String message);
    void writeErrorLog(String message);
}
