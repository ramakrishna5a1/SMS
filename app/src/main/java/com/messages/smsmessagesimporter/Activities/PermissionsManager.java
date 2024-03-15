package com.messages.smsmessagesimporter.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsManager {
    private final Activity activity;
    private static final int REQUEST_CODE_SMS_PERMISSION = 101;
    private static final int STORAGE_PERMISSION_CODE = 23;

    private final String[] smsPermissions = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS};

    public PermissionsManager(Activity activity) {
        this.activity = activity;
    }

    public boolean checkSmsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int sendSmsPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS);
            int readSmsPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS);
            return sendSmsPermission == PackageManager.PERMISSION_GRANTED && readSmsPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permissions are granted automatically on versions below M
    }

    public boolean checkStoragePermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    void handleSmsPermissionsResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // SMS permissions granted
            Toast.makeText(activity, "SMS permissions granted", Toast.LENGTH_SHORT).show();
        } else {
            // SMS permissions denied
            Toast.makeText(activity, "SMS permissions denied", Toast.LENGTH_SHORT).show();
        }
    }

}
