package experian.mobilesdk;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This interface decouples the underlying message handling from the controlling class.
 */
interface Sendable {
    boolean isSendable();
    void sendMesage(final EMSMessage msg) throws IOException;
}
