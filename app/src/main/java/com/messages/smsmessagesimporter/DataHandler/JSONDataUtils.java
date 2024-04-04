package com.messages.smsmessagesimporter.DataHandler;

import java.util.ArrayList;
import java.util.List;

public class JSONDataUtils {
    private static JSONDataUtils instance;
    private String JsonFilePath;
    private Boolean isJsonParseSuccess;
    private String jsonString;
    private int totalObjectsProcessed;
    private List<SmsEntity> smsEntityList;

    private JSONDataUtils() {
        JsonFilePath = "NULL";
        jsonString = "NULL";
        isJsonParseSuccess = true;
        totalObjectsProcessed = 0;
        smsEntityList = new ArrayList<>(200);
    }

    public static synchronized JSONDataUtils getInstance() {
        if (instance == null) {
            instance = new JSONDataUtils();
        }
        return instance;
    }

    public Boolean getJsonParseSuccess() {
        return isJsonParseSuccess;
    }

    public void setJsonParseSuccess(Boolean jsonParseSuccess) {
        isJsonParseSuccess = jsonParseSuccess;
    }

    public String getJsonFilePath() {
        return JsonFilePath;
    }

    public void setJsonFilePath(String jsonFilePath) {
        JsonFilePath = jsonFilePath;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public int getTotalObjectsProcessed() {
        return totalObjectsProcessed;
    }

    public void setTotalObjectsProcessed(int totalObjectsProcessed) {
        this.totalObjectsProcessed = totalObjectsProcessed;
    }
    public List<SmsEntity> getSmsEntityList() {
        return smsEntityList;
    }
}
