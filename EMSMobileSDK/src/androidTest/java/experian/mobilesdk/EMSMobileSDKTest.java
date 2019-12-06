package experian.mobilesdk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class EMSMobileSDKTest {
    private Context mockContext;

    @Before
    public void setUp() {
        mockContext = InstrumentationRegistry.getTargetContext();
        Assert.assertNotNull(mockContext);
    }

    @Test
    public void initializeSDK_withAppIDCustIDregion_thenExpectSameAppIDCustIDregion() {
        String appID = "0d941093-9f85-41d3-a92b-0cf205183a51";
        int customerID = 100;
        Region region = Region.NORTH_AMERICA;
        EMSMobileSDK.Default().init(mockContext, appID, customerID, region);
        assertEquals(appID, EMSMobileSDK.Default().getAppID());
        assertEquals(customerID, EMSMobileSDK.Default().getCustomerID());
        assertEquals(region, EMSMobileSDK.Default().getRegion());
    }

    @Test
    public void setDeviceToken_withGivenToken_thenExpectSameToken() {
        String appID = "0d941093-9f85-41d3-a92b-0cf205183a51";
        int customerID = 100;
        Region region = Region.NORTH_AMERICA;
        EMSMobileSDK.Default().init(mockContext, appID, customerID, region);

        String token = "fe5da804bb6167fa8a1fe44164828d5bfd853521ebc93f683de7bc4edf9a360d";
        EMSMobileSDK.Default().setToken(token);
        assertEquals(token, EMSMobileSDK.Default().getToken());
    }
}
