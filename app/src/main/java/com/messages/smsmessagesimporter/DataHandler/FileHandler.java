package com.messages.smsmessagesimporter.DataHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

public class FileHandler {

    private static final int REQUEST_PICK_FILE = 123;

    private final Activity activity;

    public FileHandler(Activity activity) {
        this.activity = activity;
    }

    public void pickFile() {
        Intent bakIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        bakIntent.addCategory(Intent.CATEGORY_OPENABLE);
        bakIntent.setType("*/*");
        String[] mimetypes = {"application/x-trash"};
        bakIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        activity.startActivityForResult(bakIntent, REQUEST_PICK_FILE);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data, JSONFileProperties jsonFileProperties) {
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String selectedFilePath = "No file selected !";
                if (uri != null) {
                    selectedFilePath = RealPathUtil.getRealPath(activity, uri);
                }
                jsonFileProperties.setJsonFilePath(selectedFilePath);
            }
        }
    }
}
