package experian.mobilesdk;

/**
 * Region Enum for CCMP Datacenters.
 */
public enum Region {
    NORTH_AMERICA(0),
    NORTH_AMERICA_SANDBOX(1),
    EMEA(2),
    JAPAN(3);

    private final int value;
    Region(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value for the Region
     * @return
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Returns the XTS Endpoint for the region
     * @return
     */
    public String getEndpoint() {
        switch (this) {
            case NORTH_AMERICA_SANDBOX:
                return "http://cs.sbox.eccmp.com";
            case EMEA:
                return "https://xts.ccmp.eu";
            case JAPAN:
                return "https://xts.ccmp.experian.co.jp";
            case NORTH_AMERICA:
            default:
                return "https://xts.eccmp.com";
        }
    }

    /**
     * Returns the API Endpoint for the region
     * @return
     */
    public String getAPIEndpoint() {
        switch (this) {
            case NORTH_AMERICA_SANDBOX:
                return "http://cs.sbox.eccmp.com/ats";
            case EMEA:
                return "https://ats.ccmp.eu/ats";
            case JAPAN:
                return "https://ats.ccmp.experian.co.jp/ats";
            case NORTH_AMERICA:
            default:
                return "https://ats.eccmp.com/ats";
        }
    }
}
