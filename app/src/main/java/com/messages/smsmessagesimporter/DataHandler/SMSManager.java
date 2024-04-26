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

import com.messages.smsmessagesimporter.Utils.SmsEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSManager implements Runnable {
    private static final String CLASS_TAG = "SMSManager";
    private Map<Integer, String> messagesNotImported;
    private final Activity activity;
    private final DataUtils dataUtils;
    private DataProcessedCallback callback;

    public SMSManager(Activity activity) {
        this.activity = activity;
        messagesNotImported = new HashMap<>();
        this.dataUtils = DataUtils.getInstance();
        callback = (DataProcessedCallback) activity;
    }

    public Map<Integer, String> getMessagesNotImported() {
        return messagesNotImported;
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint({"ResourceType", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            callback.onMessagesImported();
        }
    };

    @Override
    public void run() {
        writeToInbox();
        handler.sendEmptyMessage(0);
    }

    public Boolean writeToInbox() {
        List<SmsEntity> smsEntityList = dataUtils.getSmsEntityList();
        Log.i(CLASS_TAG, "Total Messages: " + smsEntityList.size());
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
