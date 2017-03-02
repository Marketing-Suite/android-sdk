package experian.mobilesdk;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Blaize Stewart on 1/10/2017.
 *
 * Creates a simple interfaces for reading data from persistent storage.
 */

interface Readable {

    ArrayList<EMSMessage> readQueue(Context context) throws IOException, ClassNotFoundException;
    String readToken(Context context) throws IOException;
    String readPRID(Context context) throws IOException;

}
