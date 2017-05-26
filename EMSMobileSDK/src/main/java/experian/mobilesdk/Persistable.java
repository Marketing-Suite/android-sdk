package experian.mobilesdk;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Creates a simple interfaces for writing data from persistent storage.
 */

interface Persistable {
    void persistQueue(ArrayList<EMSMessage> EMSMessages, Context context) throws IOException;

    void persistToken(String Token, Context context) throws IOException;

    void persistPRID(String PRID, Context context) throws IOException;
}