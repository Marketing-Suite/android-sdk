package experian.mobilesdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Blaize Stewart on 1/13/2017.
 *
 * This class is a wrapper  around Volley to handle the HTTP calls for the Queue.
 *
 * https://developer.android.com/training/volley/index.html
 */

class VolleySender implements Sendable {


    private Context context;
    RequestQueue queue;
    private Receivable observer;
    String url;

    public VolleySender(Context ctx) throws Exception {


        context = ctx;
        queue = Volley.newRequestQueue(ctx);



    }


    //Checks the network states and returns True if the network is available.
    @Override
    public boolean isSendable() {

        ConnectivityManager cm =  (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    //Wires up the success and error handlers for the message, then quees the message on Volley.
    @Override
    public void sendMesage(final EMSMessage msg) throws IOException {

        int m;

        switch (msg.getMethod()){
            case "GET": m = Request.Method.GET;break;
            case "POST": m = Request.Method.POST;break;
            case "DELETE": m = Request.Method.DELETE;break;
            case "PUT": m = Request.Method.PUT;break;
            case "PATCH": m = Request.Method.PATCH;break;
            default: m = Request.Method.GET;
        }



        final ServerStatusRequestObject request = new ServerStatusRequestObject(m, msg.getUrl(),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) { //The success method if the messages was successful

                        String jsonData = "";


                        //Attempts to load the message response as as string.
                        try {
                            jsonData = new String(response.data, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            //e.printStackTrace();
                        }

                        JSONObject jObj = null;

                        //Attempts to parse the response body as a JSON object.
                        try {
                            jObj = new JSONObject(jsonData);
                        } catch (JSONException e) {
                            //e.printStackTrace();
                        }

                        //Notifies the observer a message was received.
                        if (observer != null){
                            observer.onReceive(msg, response.statusCode, jObj);
                        }


                    }




                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { //The error handler
                        NetworkResponse networkResponse = error.networkResponse;
                        JSONObject jObj = new JSONObject();

                        //Attempts to parse the error message from the response
                        try {
                            jObj.put("errorMessage", error.getMessage());
                        } catch (JSONException e) {
                            //e.printStackTrace();
                        }

                        //Notifies the observer a message was received.
                        if (observer != null){
                            int code = 0;
                            if (networkResponse != null) code = networkResponse.statusCode;
                            observer.onReceive(msg, code, jObj);
                        }
                    }
        }, msg.getBody());



        queue.add(request);


    }

    public Receivable getObserver() {
        return observer;
    }

    public void setObserver(Receivable observer) {
        this.observer = observer;
    }
}
