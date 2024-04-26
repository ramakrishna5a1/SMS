package com.messages.smsmessagesimporter.DataHandler;

import com.messages.smsmessagesimporter.Utils.SmsEntity;

import java.util.ArrayList;
import java.util.List;

public class DataUtils {
    private static DataUtils instance;
    private String JsonFilePath;
    private Boolean isJsonParseSuccess;
    private String jsonString;
    private int totalObjects;
    private List<SmsEntity> smsEntityList;

    private DataUtils() {
        JsonFilePath = "NULL";
        jsonString = "NULL";
        isJsonParseSuccess = false;
        totalObjects = 0;
        smsEntityList = new ArrayList<>(200);
    }

    public void clearDataUtils() {
        JsonFilePath = "NULL";
        jsonString = "NULL";
        isJsonParseSuccess = false;
        totalObjects = 0;
        smsEntityList.clear();
    }

    public static synchronized DataUtils getInstance() {
        if (instance == null) {
            instance = new DataUtils();
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

    public int getTotalObjects() {
        return totalObjects;
    }

    public void setTotalObjects(int totalObjects) {
        this.totalObjects = totalObjects;
    }

    public List<SmsEntity> getSmsEntityList() {
        return smsEntityList;
    }
}
