package com.messages.smsmessagesimporter.DataHandler;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JsonSMSDataHandler implements Runnable {
    private final Activity activity;
    private final String filePath;

    public JsonSMSDataHandler(Activity activity, String filePath) {
        this.activity = activity;
        this.filePath = filePath;
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Log.i("SMS Parsing:", "Completed");
            Toast.makeText(activity, "Messages imported successfully", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void run() {
        try {
            String jsonString = readJsonFile(filePath);
            parseJsonArray(jsonString);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        // Notify the UI thread that parsing is completed
        handler.sendEmptyMessage(0);
    }

    private String readJsonFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    private void parseJsonArray(String jsonString) throws JSONException {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    int id = jsonObject.getInt("_id");
                    String address = jsonObject.getString("address");
                    String body = jsonObject.getString("body");
                    String date = jsonObject.getString("date");
                    int type = jsonObject.getInt("type");
                    String status = jsonObject.getString("status");
                    String subId = jsonObject.getString("sub_id");
                    int threadId = jsonObject.getInt("thread_id");
                    long dateSent = jsonObject.getLong("date_sent");

                    System.out.println("ID: " + id);
                    System.out.println("Address: " + address);
                    System.out.println("Body: " + body);
                    System.out.println("Date: " + date);
                    System.out.println("Type: " + type);
                    System.out.println("Status: " + status);
                    System.out.println("Sub ID: " + subId);
                    System.out.println("Thread ID: " + threadId);
                    System.out.println("Date Sent: " + dateSent);
                    System.out.println("-----------------------------");
                    writeToInbox(address, body, dateSent);
                } catch (Exception e) {
                    Log.e("JSON Error:", e.getMessage());
                }
            }
        }
    }

    private void writeToInbox(String phoneNumber, String messageBody, long dateSent) {
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            ContentValues values = new ContentValues();

            // Set the message values
            values.put(Telephony.Sms.ADDRESS, phoneNumber);
            values.put(Telephony.Sms.BODY, messageBody);
            values.put(Telephony.Sms.DATE, dateSent);
            values.put(Telephony.Sms.READ, 1);  // 1 means the message has been read

            // Insert the new SMS message into the inbox
            Uri uri = contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values);

            if (uri != null) {
                Log.d("SMS", "Message inserted successfully: " + uri.toString());
            } else {
                Log.e("SMS", "Failed to insert message");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
