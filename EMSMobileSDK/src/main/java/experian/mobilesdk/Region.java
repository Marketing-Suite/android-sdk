package experian.mobilesdk;

/**
 * Region Enum for CCMP Datacenters.
 */
public enum Region {
    NORTH_AMERICA(0),
    EMEA(1),
    JAPAN(2);

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
            case EMEA:
                return "https://xts.ccmp.eu";
            case JAPAN:
                return "https://xts.marketingsuite.jp";
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
            case EMEA:
                return "https://ats.ccmp.eu/ats";
            case JAPAN:
                return "https://ats.marketingsuite.jp/ats";
            case NORTH_AMERICA:
            default:
                return "https://ats.eccmp.com/ats";
        }
    }
}
