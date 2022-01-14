package experian.mobilesdk;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class RegionTest {
    @Test
    public void testRegion_NorthAmerica() {
        Region region = Region.NORTH_AMERICA;
        Assert.assertEquals(0, region.getValue());
        Assert.assertEquals("https://xts.eccmp.com", region.getEndpoint());
        Assert.assertEquals("https://ats.eccmp.com/ats", region.getAPIEndpoint());
    }

    @Test
    public void testRegion_Emea() {
        Region region = Region.EMEA;
        Assert.assertEquals(1, region.getValue());
        Assert.assertEquals("https://xts.ccmp.eu", region.getEndpoint());
        Assert.assertEquals("https://ats.ccmp.eu/ats", region.getAPIEndpoint());
    }

    @Test

    public void testRegion_Japan() {
        Region region = Region.JAPAN;
        Assert.assertEquals(2, region.getValue());
        Assert.assertEquals("https://xts.marketingsuite.jp", region.getEndpoint());
        Assert.assertEquals("https://ats.marketingsuite.jp/ats", region.getAPIEndpoint());
    }


}