package experian.mobilesdk;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * This class handles the File IO for data persistence for the SDK.
 * All data is written to persistent device storage .
 */
class AndroidIO implements Persistable, Readable {


    //Reads the Device Token from a file.
    @Override
    public String readToken(Context context) throws IOException {

        return readFile(context, "token.json");
    }

    //Reads the PRID from a file.
    @Override
    public String readPRID(Context context) throws IOException {

        return readFile(context, "prid.json");
    }

    //Writes the Message Queue to a file.
    @Override
    public void persistQueue(ArrayList<EMSMessage> EMSMessages, Context context) throws IOException {

     JSONArray jsonEMSArray = new JSONArray();
        for (int i=0; i < EMSMessages.size(); i++) {
            EMSMessage msg = EMSMessages.get(i);
            JSONObject jsonMsg = new JSONObject();

            try {
                jsonMsg.put("TimeStamp", msg.getTimeStamp());
                jsonMsg.put("URL", msg.getUrl());
                jsonMsg.put("MessageStatus", msg.getMessageStatus());
                jsonMsg.put("Body", msg.getBody());
                jsonMsg.put("ContentType", msg.getContentType());
                jsonMsg.put("Method", msg.getMethod());
                jsonEMSArray.put(jsonMsg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            writefile(context, "queue.json", jsonEMSArray.toString(3));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Generic method for wreading a file.
    private String readFile(Context context, String filename){
        FileInputStream fis = null;
        Log.d("I", "Reading: " + context.getFilesDir()+ "/" + filename);
        try {
            fis = new FileInputStream(context.getFilesDir()+ "/" + filename);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String json = new String(buffer, "UTF-8");
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    //Generic method for writing a fie.
    private void writefile(Context context, String filename, String data){
        File serialFile = new File(context.getFilesDir(), filename);
        Log.d("I", "Writing: " + context.getFilesDir()+ "/" + filename);
        try {
            FileWriter writer = new FileWriter(serialFile);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Writes the Device Token to a file.
    @Override
    public void persistToken(String Token, Context context) throws IOException {
        writefile(context, "token.json", Token);
    }

    //Writes the PRID to a file.
    @Override
    public void persistPRID(String PRID, Context context) throws IOException {
        writefile(context, "prid.json", PRID);
    }

    @Override
    public ArrayList<EMSMessage> readQueue(Context context) throws IOException, ClassNotFoundException {

        ArrayList<EMSMessage> queue = new ArrayList<EMSMessage>();

        String json = readFile(context, "queue.json");

        JSONArray qObj = new JSONArray();

        try {
            qObj = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < qObj.length(); i++){
            EMSMessage msg = new EMSMessage();
            try {
                JSONObject jMsg = qObj.getJSONObject(i);
                msg.setMessageStatus(MessageStatus.valueOf(jMsg.getString("MessageStatus")));
                msg.setUrl(jMsg.getString("URL"));
                msg.setTimeStamp((long)jMsg.getDouble("TimeStamp"));
                msg.setMethod(jMsg.getString("Method"));
                msg.setContentType(jMsg.getString("ContentType"));
                msg.setBody(jMsg.getString("Body"));
                queue.add(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return queue;
    }

}
