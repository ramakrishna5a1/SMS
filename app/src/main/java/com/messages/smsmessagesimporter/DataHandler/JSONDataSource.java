package com.messages.smsmessagesimporter.DataHandler;

import android.app.Activity;

public abstract class JSONDataSource {
    protected final Activity activity;
    protected final DataUtils dataUtils;
    protected DataProcessedCallback callback;

    public JSONDataSource(Activity activity) {
        this.activity = activity;
        this.dataUtils = DataUtils.getInstance();
        callback = (DataProcessedCallback) activity;
    }

    protected abstract void readJsonStringFromSource();
}