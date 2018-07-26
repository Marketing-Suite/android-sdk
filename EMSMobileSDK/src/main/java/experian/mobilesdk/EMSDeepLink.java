package experian.mobilesdk;

import android.content.Intent;
import android.widget.TextView;

public class EMSDeepLink {
    private String deepLinkParameter;
    private String deepLinkUrl;

    public EMSDeepLink(Intent intent){
        if(intent.getData() != null){
            deepLinkParameter = intent.getData().getQueryParameter("dl") != null ?
                intent.getData().getQueryParameter("dl") : "";
            deepLinkUrl = intent.getData().toString();
        }
    }

    public String getDeepLinkParameter(){
        return deepLinkParameter;
    }

    public String getDeepLinkUrl(){
        return deepLinkUrl;
    }
}