package com.messages.smsmessagesimporter.DataHandler;

import com.messages.smsmessagesimporter.Utils.SmsEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataUtils {
    private static DataUtils instance;
    private String JsonSourcePath;
    private Boolean isJsonParseSuccess;
    private String jsonString;
    private int totalObjects;
    public List<SmsEntity> smsEntityList;
    public Set<Integer> insertedMessageIndexes;

    private DataUtils() {
        JsonSourcePath = "NULL";
        jsonString = "NULL";
        isJsonParseSuccess = false;
        totalObjects = 0;
        smsEntityList = new ArrayList<>(500);
        insertedMessageIndexes = new HashSet<>(500);
    }

    public void clearDataUtils() {
        JsonSourcePath = "NULL";
        jsonString = "NULL";
        isJsonParseSuccess = false;
        totalObjects = 0;
        smsEntityList.clear();
        insertedMessageIndexes.clear();
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

    public String getJsonSourcePath() {
        return JsonSourcePath;
    }

    public void setJsonSourcePath(String jsonSourcePath) {
        JsonSourcePath = jsonSourcePath;
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
}
