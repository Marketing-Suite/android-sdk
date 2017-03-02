package experian.mobilesdk;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by Blaize Stewart on 1/10/2017.
 *
 * This interface decouples the underlying message handling from the controlling class.
 */


interface Sendable {
    boolean isSendable();
    void sendMesage(final EMSMessage msg) throws IOException;
}
