package experian.mobilesdk;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Blaize Stewart on 1/10/2017.
 *
 * Creates a simple interfaces for writing data from persistent storage.
 */

interface Persistable {
    void persistQueue(ArrayList<EMSMessage> EMSMessages, Context context) throws IOException;

    void persistToken(String Token, Context context) throws IOException;

    void persistPRID(String PRID, Context context) throws IOException;
}