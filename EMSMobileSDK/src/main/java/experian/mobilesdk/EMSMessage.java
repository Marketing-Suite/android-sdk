package experian.mobilesdk;

import java.util.Date;

/**
 * Created by Blaize Stewart on 1/10/2017.
 *
 * This class services as a DTO for Messages that are sent to the Queue.
 */

 class EMSMessage {

    private long timeStamp;
    private MessageStatus messageStatus;
    private String url;
    private String method;
    private Object body;
    private IEMSCallback callback;


    public EMSMessage(){
        timeStamp = System.currentTimeMillis();
        messageStatus = MessageStatus.NEW;
    }

    //Time stamp for the message
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


    //Message Status for processing.
    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    //The intended URL for the message to call.
    public String getUrl() {
        return url;
    }

    public void setUrl(String path) {
        this.url = path;
    }


    //The HTTP Method to use with the call.
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


    //The Body of the message.
    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }


    //Callback hook for the message. These are lost if the application is killed or shut down.
    public IEMSCallback getCallback() {
        return callback;
    }

    public void setCallback(IEMSCallback callback) {
        this.callback = callback;
    }
}