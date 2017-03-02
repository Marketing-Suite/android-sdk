package experian.mobilesdk;

/**
 * Created by Blaize Stewart on 1/10/2017.
 *
 * These are the statuses used by the Message Queue.
 */

enum MessageStatus {
    NEW(0),
    WAIT5SEC(1),
    WAIT30SEC(2),
    WAIT5MIN(3),
    SENT(5),
    FAIL(4);

    private final int msgStatus;

    private MessageStatus(int ms) {
        this.msgStatus = ms;
    }
    public int getValue() { return msgStatus; }

    public MessageStatus getNext() {
        return values()[(ordinal()+1) % values().length];
    }

}
