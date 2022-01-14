package experian.mobilesdk;

/**
 * Interface which handles the receiving of push registration id
 */
public interface IEMSPRIDCallback {
    /**
     * This is called when PRID is received from the server
     * @param PRID push registration id
     */
    public void onPRIDReceived(String PRID);
}
