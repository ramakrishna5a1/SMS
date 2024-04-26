package com.messages.smsmessagesimporter.Utils;

import static android.app.role.RoleManager.ROLE_SMS;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Telephony;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class PermissionsHandler {
    private static final int REQUEST_CODE_SMS_PERMISSION = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int DEFAULT_SMS_APP_PERMISSION_CODE = 102;
    private static final int NETWORK_PERMISSION = 103;

    private final String[] smsPermissions = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS};
    private boolean[] allPermissionsGranted = new boolean[3];

    private final FragmentActivity activity;
    private final ActivityResultLauncher<Intent> storageActivityResultLauncher;

    public PermissionsHandler(FragmentActivity activity, ActivityResultLauncher<Intent> storageActivityResultLauncher) {
        this.activity = activity;
        this.storageActivityResultLauncher = storageActivityResultLauncher;
    }

    public void checkAndRequestPermissions() {
        allPermissionsGranted[0] = checkSmsPermissions();
        allPermissionsGranted[1] = checkStoragePermissions();
        allPermissionsGranted[2] = checkDefaultSmsAppPermissions();

        if (!allPermissionsGranted[0]) {
            requestSmsPermissions();
            allPermissionsGranted[0] = checkSmsPermissions();
        }
        if (!allPermissionsGranted[1]) {
            requestForStoragePermissions();
            allPermissionsGranted[1] = checkStoragePermissions();
        }
        if (!allPermissionsGranted[2]) {
            askDefaultSmsHandlerPermission();
            allPermissionsGranted[2] = checkDefaultSmsAppPermissions();
        }
        if (!checkNetworkPermissions()) {
            requestNetworkPermission();
        }
    }

    public void updatePermissionsText(TextView permissionsView) {
        String permissionsGranted = "";

        if (allPermissionsGranted[0] && allPermissionsGranted[1] && allPermissionsGranted[2]) {
            permissionsGranted = permissionsGranted.concat("All permissions are granted !");
            permissionsView.setTextColor(Color.BLUE);
        } else {
            permissionsGranted = permissionsGranted.concat("Grant below permissions:\n");
            if(!allPermissionsGranted[0]) permissionsGranted = permissionsGranted.concat("- SMS Permission\n");
            if(!allPermissionsGranted[1]) permissionsGranted = permissionsGranted.concat("- Storage Permission\n");
            if(!allPermissionsGranted[2]) permissionsGranted = permissionsGranted.concat("- Default SMS App Permission\n");
            permissionsGranted = permissionsGranted.concat("Note: Please close and open App Again.!!");
            permissionsView.setTextColor(Color.RED);
        }
        permissionsView.setText(permissionsGranted);
    }

    private boolean checkDefaultSmsAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = activity.getSystemService(RoleManager.class);
            return roleManager.isRoleHeld(RoleManager.ROLE_SMS);
        } else {
            return activity.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(activity));
        }
    }

    private boolean checkSmsPermissions() {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int sendSmsPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS);
            int readSmsPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS);
            result = sendSmsPermission == PackageManager.PERMISSION_GRANTED && readSmsPermission == PackageManager.PERMISSION_GRANTED;
        } else {
            result = true;
        }
        return result;
    }

    private boolean checkNetworkPermissions() {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int internetPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET);
            result = internetPermission == PackageManager.PERMISSION_GRANTED;
        }
        return result;
    }

    private void requestNetworkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int internetPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET);
            if (internetPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.INTERNET}, NETWORK_PERMISSION);
            }
        }
    }

    private void requestSmsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, smsPermissions, REQUEST_CODE_SMS_PERMISSION);
        }
    }

    private void askDefaultSmsHandlerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = activity.getSystemService(RoleManager.class);
            String role = ROLE_SMS;
            boolean isRoleAvailable = roleManager.isRoleAvailable(role);
            if (isRoleAvailable) {
                boolean isRoleHeld = roleManager.isRoleHeld(role);
                if (!isRoleHeld) {
                    Intent intent = roleManager.createRequestRoleIntent(role);
                    activity.startActivityForResult(intent, DEFAULT_SMS_APP_PERMISSION_CODE);
                }
            }
        } else {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.getPackageName());
            activity.startActivityForResult(intent, DEFAULT_SMS_APP_PERMISSION_CODE);
        }
    }

    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    public void handleSmsPermissionsResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // SMS permissions granted
            Toast.makeText(activity, "SMS permissions granted", Toast.LENGTH_SHORT).show();
        } else {
            // SMS permissions denied
            Toast.makeText(activity, "SMS permissions denied", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleDefaultSmsAppPermissionResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(activity, "Success requesting ROLE_SMS!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Failed requesting ROLE_SMS!", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleStoragePermissionsResult(int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (checkStoragePermissions()) {
                Toast.makeText(activity, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleNetworkPermissionsResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, "Internet Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Internet Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}
