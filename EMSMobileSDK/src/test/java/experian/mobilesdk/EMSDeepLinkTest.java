package experian.mobilesdk;

import android.content.Intent;
import android.net.Uri;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class EMSDeepLinkTest {
    private String testUrl = "http://rts.eccmp.com/rts/go2.aspx?dl=param&page=main&data=url";
    private Intent intent;
    private EMSDeepLink emsDeepLink;

    @Before
    public void setUp() {
        intent = new Intent();
    }

    @Test
    public void testGetDeepLinkUrl_isUrlNotNull() {
        intent.setData(Uri.parse(testUrl));
        emsDeepLink = new EMSDeepLink(intent);
        String url = emsDeepLink.getDeepLinkUrl();
        Assert.assertEquals(testUrl, url);
    }

    @Test
    public void testGetDeepLinkUrl_isUrlNull() {
        intent.setData(null);
        emsDeepLink = new EMSDeepLink(intent);
        String url = emsDeepLink.getDeepLinkUrl();
        Assert.assertEquals("", url);
    }

    @Test
    public void testGetQueryParameters_isParametersDecoded() {
        intent.setData(Uri.parse(testUrl));
        emsDeepLink = new EMSDeepLink(intent);
        HashMap<String, String> queryParams = emsDeepLink.getQueryParameters();
        Assert.assertEquals(3, queryParams.size());

        HashMap<String, String> expectedQueryParams = new HashMap<>();
        expectedQueryParams.put("dl", "param");
        expectedQueryParams.put("page", "main");
        expectedQueryParams.put("data", "url");

        Assert.assertEquals(expectedQueryParams, queryParams);
    }

    @Test
    public void testGetQueryParameter_isValueDecoded() {
        intent.setData(Uri.parse(testUrl));
        emsDeepLink = new EMSDeepLink(intent);
        String queryParam = emsDeepLink.getQueryParameter("dl");
        Assert.assertEquals("param", queryParam);
    }
}