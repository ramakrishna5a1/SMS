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
import android.widget.TextView;
import android.widget.Toast;

import com.messages.smsmessagesimporter.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JsonSMSDataHandler implements Runnable {
    private final Activity activity;
    private JSONFileProperties jsonFileProps;
    private TextView numOfMessagesWritten = null;
    private TextView jsonFileParsing = null;

    public JsonSMSDataHandler(Activity activity, JSONFileProperties jsonFileProperties) {
        this.activity = activity;
        this.jsonFileProps = jsonFileProperties;
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            numOfMessagesWritten = activity.findViewById(R.id.num_messages_written);
            jsonFileParsing = activity.findViewById(R.id.file_parsing);

            if (jsonFileProps.getJsonParseSuccess()) {
                if (jsonFileProps.getTotalMessagesImported() == jsonFileProps.getTotalMessages()) {
                    Toast.makeText(activity, "Messages imported successfully", Toast.LENGTH_SHORT).show();
                } else if (jsonFileProps.getTotalMessagesImported() > 0) {
                    Toast.makeText(activity, "Messages imported partially", Toast.LENGTH_SHORT).show();
                }
            }
            jsonFileParsing.setText("File parsing: "+(jsonFileProps.getJsonParseSuccess() ? "Success" : "Failure"));
            numOfMessagesWritten.setText("Total Messages Written: " + jsonFileProps.getTotalMessagesImported() + "/" + jsonFileProps.getTotalMessages());
        }
    };

    @Override
    public void run() {
        try {
            String jsonString = readJsonFile(jsonFileProps.getJsonFilePath());
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
            jsonFileProps.setJsonParseSuccess(false);
        }

        if (jsonArray != null) {
            jsonFileProps.setTotalMessages(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                boolean isMessageWritten = true;
                String errorMessage = "No error !";
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                try {
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

                    isMessageWritten = writeToInbox(id, address, body, dateSent);
                    if (!isMessageWritten) errorMessage = "Message not imported";
                } catch (Exception e) {
                    Log.e("JSON Error:", e.getMessage());
                    errorMessage = e.getMessage();
                    isMessageWritten = false;
                }
                if (!isMessageWritten) {
                    jsonFileProps.setMessageToNotImported((i + 1), errorMessage);
                }
            }
            jsonFileProps.setTotalMessagesImported(jsonFileProps.getTotalMessages() - jsonFileProps.getNumOfMsgNotImported());
        }
    }

    private Boolean writeToInbox(int messageId, String phoneNumber, String messageBody, long dateSent) {
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

            if (uri == null) {
                Log.e("SMS", "Failed to insert message:" + messageId);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
