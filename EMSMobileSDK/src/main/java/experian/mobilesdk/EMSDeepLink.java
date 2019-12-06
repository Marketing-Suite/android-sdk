package experian.mobilesdk;


import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;


/**
 * Class for handling deep links
 */
public class EMSDeepLink {
    private Intent mIntent;

    /**
     * Constructor for Deep link
     *
     * @param intent deeplink intent
     */
    public EMSDeepLink(Intent intent) {
        mIntent = intent;
    }

    /**
     * Get deep link url from the intent
     *
     * @return Original Deep link URL; example value: "http://rts.eccmp.com/rts/go2.aspx?dl=param"
     */
    public String getDeepLinkUrl() {
        return mIntent.getData().toString();
    }

    /**
     * get decoded parameter values from CCMP if any in hashmap format
     * example:  http://rts.eccmp.com/rts/go2.aspx?dl=param&page=main&data=url"
     *
     * @return the decoded values in hashmap format example: hashmap data of (dl,param),(page,main),(data,url)
     */
    public HashMap<String, String> getQueryParameters() {
        HashMap<String, String> params = new HashMap<>();
        Uri uri = mIntent.getData();
        if (uri != null) {
            for (String key : uri.getQueryParameterNames()) {
                params.put(key, uri.getQueryParameter(key));
            }
        }
        return params;

    }

    /**
     * get decoded parameter value from CCMP if any;
     * example: http://rts.eccmp.com/rts/go2.aspx?dl=param" url link
     *
     * @param name which will be encoded. Example value: dl
     * @return the decoded value or null if no parameter is found. Example value: param
     */
    public String getQueryParameter(String name) {
        return mIntent.getData().getQueryParameter(name);
    }

}