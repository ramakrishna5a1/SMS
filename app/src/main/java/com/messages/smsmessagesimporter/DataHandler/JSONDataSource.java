package com.messages.smsmessagesimporter.DataHandler;

import android.app.Activity;

public abstract class JSONDataSource {
    protected final Activity activity;
    protected final JSONDataUtils jsonDataUtils;
    protected DataProcessedCallback callback;

    public JSONDataSource(Activity activity) {
        this.activity = activity;
        this.jsonDataUtils = JSONDataUtils.getInstance();
        callback = (DataProcessedCallback) activity;
    }
}