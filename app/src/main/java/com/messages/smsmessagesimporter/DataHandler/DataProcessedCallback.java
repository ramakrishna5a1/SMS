package com.messages.smsmessagesimporter.DataHandler;

public interface DataProcessedCallback {
    void onJSONSourceSelected();
    void onJSONDataReadDone();
    void onJSONDataProcessed();
    void onMessagesImported();
}
