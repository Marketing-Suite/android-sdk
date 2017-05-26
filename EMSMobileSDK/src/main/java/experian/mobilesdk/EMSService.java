package experian.mobilesdk;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static java.lang.Process.*;

/**
 * This class acts a background process on Android to handle the Message Queue to ensure that
 * messages get delivered.
 */
public class EMSService extends Service  {
    private static final String TAG = "EMSService";

    private MessageQueue mq;

    private NotificationManager mNM;

    public class EMSBinder extends Binder {
        EMSService getService() {
            return EMSService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mq = MessageQueue.Default();
        try {
            mq.init(getApplicationContext());
        } catch (Exception e) {

        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new EMSBinder();


    //Queued messages are passed through the service to the underlying Message Queue.
    protected void QueueMessage(EMSMessage msg){
        mq.queueMessage(msg);

    }
}