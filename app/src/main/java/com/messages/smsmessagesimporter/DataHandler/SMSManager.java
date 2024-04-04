package com.messages.smsmessagesimporter.DataHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSManager implements Runnable {
    private int totalMessages;
    private int totalMessagesImported;
    private Map<Integer, String> messagesNotImported;
    private final Activity activity;
    private final JSONDataUtils jsonDataUtils;

    public SMSManager(Activity activity) {
        this.activity = activity;
        totalMessages = 0;
        totalMessagesImported = 0;
        messagesNotImported = new HashMap<>();
        this.jsonDataUtils = JSONDataUtils.getInstance();
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public int getTotalMessagesImported() {
        return totalMessagesImported;
    }

    public Map<Integer, String> getMessagesNotImported() {
        return messagesNotImported;
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint({"ResourceType", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            writeToInbox();
        }
    };

    @Override
    public void run() {
        writeToInbox();
    }

    public Boolean writeToInbox() {
        List<SmsEntity> smsEntityList = jsonDataUtils.getSmsEntityList();
        Log.i("SMS List Size:", "" + smsEntityList.size());
        for (SmsEntity smsEntity : smsEntityList) {
            try {
                ContentResolver contentResolver = activity.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(Telephony.Sms.ADDRESS, smsEntity.getAddress());
                values.put(Telephony.Sms.BODY, smsEntity.getBody());
                values.put(Telephony.Sms.DATE, smsEntity.getDate());
                values.put(Telephony.Sms.READ, 1);  // 1 means the message has been read

                // Insert the new SMS message into the inbox
                Uri uri = contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values);
                if (uri != null) {
                    // Extract the ID from the URI
                    long insertedId = Long.parseLong(uri.getLastPathSegment());
                    Log.d("SMS", "Inserted ID: " + insertedId);
                } else {
                    Log.e("SMS", "URI is NULL");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
