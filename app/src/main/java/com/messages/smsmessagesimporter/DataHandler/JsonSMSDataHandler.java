package com.messages.smsmessagesimporter.DataHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        @SuppressLint({"ResourceType", "SetTextI18n"})
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
            jsonFileParsing.setText(activity.getResources().getString(R.string.file_parsing) + " " +
                    (jsonFileProps.getJsonParseSuccess() ? "Success" : "Failure"));
            numOfMessagesWritten.setText(activity.getResources().getString(R.string.number_of_messages_written) + " " +
                    jsonFileProps.getTotalMessagesImported() + "/" + jsonFileProps.getTotalMessages());
        }
    };

    @Override
    public void run() {
        try {
            String jsonString = readJsonFile(jsonFileProps.getJsonFilePath());
            parseJsonArray(jsonString);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            jsonFileProps.setJsonParseSuccess(false);
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
                Log.i("Processing Message: ", "Json Message object: " + (i + 1));

                boolean isMessageWritten = true;
                String errorMessage = "No error !";
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                try {
                    int id = 0;//jsonObject.getInt("_id");
                    String address = jsonObject.getString("address");
                    String body = jsonObject.getString("body");
                    /*
                     * Date format from JSON data is "dd/MM/yyyy hh:mm:ss a"
                     * So convert it into long before writing to inbox
                     * */
                    String dateString = jsonObject.getString("date");

                    final String dateFormatPattern = "dd/MM/yyyy hh:mm:ss a";
                    DateFormat formatter = new SimpleDateFormat(dateFormatPattern, Locale.getDefault());
                    // Parse the date string into a Date object
                    Date date = formatter.parse(dateString);
                    // Get the milliseconds from the Date object
                    long dateInMillis = date.getTime();

                    isMessageWritten = writeToInbox(id, address, body, dateInMillis);
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

            values.put(Telephony.Sms.ADDRESS, phoneNumber);
            values.put(Telephony.Sms.BODY, messageBody);
            values.put(Telephony.Sms.DATE, dateSent);
            values.put(Telephony.Sms.READ, 1);  // 1 means the message has been read

            // Insert the new SMS message into the inbox
            Uri uri = contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values);
            if (uri != null) {
                // Extract the ID from the URI
                long insertedId = Long.parseLong(uri.getLastPathSegment());
                Log.d("SMS", "Message inserted successfully with ID: " + insertedId);
                return insertedId != 0;
            } else {
                Log.e("SMS", "URI is NULL");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("Range")
    public long getThreadIdForMessage(long messageId) {
        long threadId = -1;
        Cursor cursor = activity.getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                new String[]{Telephony.Sms.THREAD_ID},
                Telephony.Sms._ID + " = ?",
                new String[]{String.valueOf(messageId)},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            threadId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.THREAD_ID));
            cursor.close();
        }

        return threadId;
    }
}
