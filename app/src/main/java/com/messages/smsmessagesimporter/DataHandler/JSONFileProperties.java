package com.messages.smsmessagesimporter.DataHandler;

import java.util.HashMap;
import java.util.Map;

public class JSONFileProperties {
    private String JsonFilePath;
    private int totalMessages;
    private int totalMessagesImported;
    private Map<Integer, String> messagesNotImported;
    private Boolean isJsonParseSuccess;

    public JSONFileProperties() {
        messagesNotImported = new HashMap<>();
        isJsonParseSuccess = true;
        totalMessages = 0;
        JsonFilePath = "NULL";
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

    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getTotalMessagesImported() {
        return totalMessagesImported;
    }

    public void setTotalMessagesImported(int totalMessagesImported) {
        this.totalMessagesImported = totalMessagesImported;
    }

    public void setMessageToNotImported(final int messageId, final String errorMessage) {
        messagesNotImported.put(messageId, errorMessage);
    }

    public int getNumOfMsgNotImported(){
        return messagesNotImported.size();
    }
}
