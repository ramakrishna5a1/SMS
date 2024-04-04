package com.messages.smsmessagesimporter.DataHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JSONFileHandler extends JSONDataSource{

    private static final int REQUEST_PICK_FILE = 123;

    public JSONFileHandler(Activity activity) {
        super(activity);
    }

    public void pickFile() {
        Intent bakIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        bakIntent.addCategory(Intent.CATEGORY_OPENABLE);
        bakIntent.setType("*/*");
        String[] mimetypes = {"application/x-trash"};
        bakIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        activity.startActivityForResult(bakIntent, REQUEST_PICK_FILE);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String selectedFilePath = "NULL";
                if (uri != null) {
                    selectedFilePath = RealPathUtil.getRealPath(activity, uri);
                }
                jsonDataUtils.setJsonFilePath(selectedFilePath);
                Log.i("Selected File Path:", jsonDataUtils.getJsonFilePath());

                // Start a new thread to read the file content
                new Thread(() -> {
                    try {
                        readFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    private void readFile() throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(jsonDataUtils.getJsonFilePath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }
        jsonDataUtils.setJsonString(content.toString());
        // If you need to update UI after reading the file, use Handler to post a Runnable on the UI thread
        new Handler(Looper.getMainLooper()).post(() -> {
            callback.onJSONDataReadDone();
        });
    }
}