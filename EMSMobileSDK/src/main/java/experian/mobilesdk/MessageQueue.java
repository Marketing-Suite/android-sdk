package experian.mobilesdk;

import android.content.Context;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles the message queue to help improve the success of message deliveries.
 *
 * The rules are:
 *
 * If the Message doesn't send, wait 5 seconds, 30 seconds, then 5 minutes -- then give up.
 * Attempts are only made if the network is available.
 * Messages are persisted to device storage.
 * The processing of the Queue is done every five seconds o a timer.
 */
class MessageQueue implements Receivable {

    // TODO: put back
    static final int FIVESECONDS = 5000;
    static final int THIRTYSECONDS = 30000;
    static final int FIVEMINUTES = 300000;

    private ArrayList<EMSMessage> EMSMessageArrayList;
    private Persistable queueWriter;
    private Loggable logWriter;
    private Readable queueReader;
    private Sendable sender;
    private Timer timer;
    private Context context;
    private static MessageQueue instance = null; //singleton instance of the queue
    private ConnectionReceiver cr;

    protected MessageQueue() {
    }
    public static MessageQueue Default() { //Gets and instance of the queue
        if(instance == null) {
            instance = new MessageQueue();
        }
        return instance;
    }

    //initializes teh Queue
    public void initialize(Loggable lw, Persistable qw, Readable qr, Sendable s){
        logWriter  = lw;
        queueWriter = qw;
        queueReader = qr;
        sender = s;

        timer = new Timer();


        try{
            EMSMessageArrayList = queueReader.readQueue(context); //reads the queue from storage.
        }catch(Exception e){
            EMSMessageArrayList = new ArrayList<EMSMessage>();
        }

        scheduleTimer();

        cr = new ConnectionReceiver();


    }

    public void init(Context context) throws Exception {

        this.context = context;

        VolleySender sender;

        try {
            sender = new VolleySender(context);
        } catch (Exception e) {
            throw e;
        }

        sender.setObserver(this);

        initialize(new AndroidLogger(),  new AndroidIO(),  new AndroidIO(), sender);
    }

    //Schedules the timer task.
    private void scheduleTimer(){
        TimerTask queueTask = new TimerTask() {

            @Override
            public void run() {
                ProcessQueue();
                scheduleTimer();
            }

        };
        timer.schedule(queueTask, FIVESECONDS);
    }

    //Messages are added to the queue and persisted.
    public void queueMessage(EMSMessage msg){
        EMSMessageArrayList.add(msg);

        try{
            queueWriter.persistQueue(EMSMessageArrayList, context); //persists the queue.
        }catch(Exception e){
            logWriter.writeErrorLog("Unable to persist queue " + System.currentTimeMillis() + " " + e.getMessage());
        }

    }

    //Processes the Queue
    private void ProcessQueue(){
        logWriter.writeLog("Processing Queue at " + System.currentTimeMillis());

        long now = System.currentTimeMillis();

        if(sender.isSendable()){ //isSendable if the netowrk is available.
            for(EMSMessage msg : EMSMessageArrayList) {

                long dateDiff = now - msg.getTimeStamp();
                boolean sendit = false;

                if (dateDiff < FIVESECONDS && msg.getMessageStatus().getValue() < MessageStatus.WAIT5SEC.getValue()){ //New message
                    sendit = true;
                }else if (dateDiff >= FIVESECONDS && dateDiff < THIRTYSECONDS  && msg.getMessageStatus().getValue() < MessageStatus.WAIT30SEC.getValue()){ //Waiting 5 seconds
                    sendit = true;
                }else if (dateDiff >= THIRTYSECONDS && dateDiff < FIVEMINUTES  && msg.getMessageStatus().getValue() < MessageStatus.WAIT5MIN.getValue()){ //Waiting 30 seconds
                    sendit = true;
                }else if (dateDiff >= FIVEMINUTES  && msg.getMessageStatus().getValue() < MessageStatus.FAIL.getValue()){ // Waiting 5 minutes
                    sendit = true;
                }

                if (sendit){
                    try{

                        sender.sendMesage(msg);

                    }catch(Exception e){
                        logWriter.writeErrorLog(e.getMessage());
                        msg.setMessageStatus(msg.getMessageStatus().getNext());
                    }
                }

            }
        }
    }

    //Messages are handled by way of the HTTP Sender. This the callback method for HTTP messages.
    @Override
    public void onReceive(EMSMessage msg, int responseCode, String responseData) {

        switch (responseCode) {
            case 200: //Message OK -- Sent
                msg.setMessageStatus(MessageStatus.SENT);
                logWriter.writeLog("EMSMessage sent");
                break;
            case 201: //Object Created -- Sent
                msg.setMessageStatus(MessageStatus.SENT);
                logWriter.writeLog("EMSMessage sent:");
                break;
            case 400: //Bad Message -- Fail
                msg.setMessageStatus(MessageStatus.FAIL);
                logWriter.writeErrorLog("EMSMessage failed to send: 400: The Request was submitted incorrectly.");
                break;
            case 401: //Authentication Failed -- Fail
                msg.setMessageStatus(MessageStatus.FAIL);
                logWriter.writeErrorLog("EMSMessage failed to send: 401: You must be Authenticated to use this resource, please supply a Bearer token with the request.");
                break;
            case 403: //Authorization Failed -- Fail
                msg.setMessageStatus(MessageStatus.FAIL);
                logWriter.writeErrorLog("EMSMessage failed to send: 403: The user is authenticated correctly, however the user does not have the proper permissions in the system to execute this request.");
                break;
            case 404: //Not Found -- Next Status and Try again if not Fail
                msg.setMessageStatus(msg.getMessageStatus().getNext());
                logWriter.writeErrorLog("EMSMessage failed to send: 404: Not Found. Will retry again later.");
                break;
            case 500: //Server Error -- Next Status and Try again if not Fail
                msg.setMessageStatus(msg.getMessageStatus().getNext());
                logWriter.writeErrorLog("EMSMessage failed to send: 500: An internal processing error has occurred. Will retry again later.");
                break;
            default: //Unknown Error -- Next Status and Try again if not Fail
                msg.setMessageStatus(MessageStatus.FAIL);
                logWriter.writeErrorLog("EMSMessage failed to send: " + responseCode + ": Unknown error.");
                break;
        }


        //purges sent or failed messages
        for(int i = EMSMessageArrayList.size() -1; i >= 0; i--) {
            EMSMessage m = EMSMessageArrayList.get(i);
            if (m.getMessageStatus() == MessageStatus.FAIL || m.getMessageStatus() == MessageStatus.SENT){
                EMSMessageArrayList.remove(i);
                IEMSCallback cb = m.getCallback();
                if (cb != null){
                    m.getCallback().Callback(responseData);
                }
            }
        }

        try{
            queueWriter.persistQueue(EMSMessageArrayList, context); //persists the queue after processing.
        }catch(Exception e){
            logWriter.writeErrorLog("Unable to persist queue " + System.currentTimeMillis() + " " + e.getMessage());
        }

    }
}
