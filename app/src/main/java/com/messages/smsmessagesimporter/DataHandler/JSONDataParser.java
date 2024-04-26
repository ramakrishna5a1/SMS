package com.messages.smsmessagesimporter.DataHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.messages.smsmessagesimporter.Utils.SmsEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JSONDataParser implements Runnable {
    private static final String CLASS_TAG = "JSONDataParser";
    private final Activity activity;
    private final DataUtils dataUtils;
    private DataProcessedCallback callback;

    public JSONDataParser(Activity activity) {
        this.activity = activity;
        callback = (DataProcessedCallback) activity;
        this.dataUtils = DataUtils.getInstance();
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint({"ResourceType", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            callback.onJSONDataProcessed();
        }
    };

    @Override
    public void run() {
        try {
            parseJsonArray(dataUtils.getJsonString());
        } catch (JSONException e) {
            dataUtils.setJsonParseSuccess(false);
            e.printStackTrace();
        }
        handler.sendEmptyMessage(0);
    }

    private void parseJsonArray(String jsonString) throws JSONException {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonArray != null) {
            dataUtils.getSmsEntityList().clear();
            dataUtils.setTotalObjects(jsonArray.length());
            Log.i(CLASS_TAG, "Json Object Count: " + dataUtils.getTotalObjects());
            for (int jsonObjIdx = 0; jsonObjIdx < jsonArray.length(); ++jsonObjIdx) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(jsonObjIdx);

                    String address = jsonObject.getString("address");
                    String body = jsonObject.getString("body");
                    /*
                     * Date format from JSON data is "dd/MM/yyyy hh:mm:ss a"
                     * So convert it into long
                     * */
                    String dateString = jsonObject.getString("date");
                    final String dateFormatPattern = "dd/MM/yyyy hh:mm:ss a";
                    DateFormat formatter = new SimpleDateFormat(dateFormatPattern, Locale.getDefault());
                    // Parse the date string into a Date object
                    Date date = formatter.parse(dateString);
                    // Get the milliseconds from the Date object
                    long dateInMillis = date.getTime();
                    SmsEntity singleEntity = new SmsEntity(address, body, dateInMillis, dateString);
                    dataUtils.getSmsEntityList().add(singleEntity);
                } catch (Exception e) {
                    Log.e("JSON Error:", e.getMessage());
                }
            }
        }
    }
}
