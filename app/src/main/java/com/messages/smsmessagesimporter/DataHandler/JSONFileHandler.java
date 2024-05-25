package com.messages.smsmessagesimporter.DataHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.messages.smsmessagesimporter.Utils.RealPathUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JSONFileHandler extends JSONDataSource {
    private final String CLASS_TAG = "JSONFileHandler";
    private static final int REQUEST_PICK_FILE = 104;

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
                dataUtils.setJsonSourcePath(selectedFilePath);
                Log.i("Selected File Path:", dataUtils.getJsonSourcePath());

                // Start a new thread to read the file content
                new Thread(this::readJsonStringFromSource).start();
            }
        }
    }

    public void readJsonStringFromSource() {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(dataUtils.getJsonSourcePath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            Log.e(CLASS_TAG,e.getMessage());
            //throw new RuntimeException(e);
        }
        dataUtils.setJsonString(content.toString());
        new Handler(Looper.getMainLooper()).post(() -> {
            callback.onJSONDataReadDone();
        });
    }
}