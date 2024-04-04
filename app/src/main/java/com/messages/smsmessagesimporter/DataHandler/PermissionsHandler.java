package com.messages.smsmessagesimporter.DataHandler;

import static android.app.role.RoleManager.ROLE_SMS;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

    private final String[] smsPermissions = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS};

    private final FragmentActivity activity;
    private final ActivityResultLauncher<Intent> storageActivityResultLauncher;

    public PermissionsHandler(FragmentActivity activity, ActivityResultLauncher<Intent> storageActivityResultLauncher) {
        this.activity = activity;
        this.storageActivityResultLauncher = storageActivityResultLauncher;
    }

    public void checkAndRequestPermissions() {
        if (!checkSmsPermissions()) {
            requestSmsPermissions();
        }
        if (!checkStoragePermissions()) {
            requestForStoragePermissions();
        }
        if (!checkDefaultSmsAppPermissions()) {
            askDefaultSmsHandlerPermission();
        }
    }

    public void updatePermissionsText(TextView permissionsView) {
        String permissionsGranted = "";

        boolean[] allPermissionsGranted = {
                checkSmsPermissions(),
                checkStoragePermissions(),
                checkDefaultSmsAppPermissions()
        };

        if (allPermissionsGranted[0] && allPermissionsGranted[1] && allPermissionsGranted[2]) {
            permissionsGranted = permissionsGranted.concat("All permissions are granted !");
            permissionsView.setTextColor(Color.BLUE);
        } else {
            permissionsGranted = permissionsGranted.concat(String.format("SMS Permission %s\n", (allPermissionsGranted[0] ? "Granted" : "Not granted")));
            permissionsGranted = permissionsGranted.concat(String.format("Storage Permission %s\n", (allPermissionsGranted[1] ? "Granted" : "Not granted")));
            permissionsGranted = permissionsGranted.concat(String.format("Default SMS App Permission %s\n\n", (allPermissionsGranted[2] ? "Granted" : "Not granted")));
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

    private void requestSmsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, smsPermissions, REQUEST_CODE_SMS_PERMISSION);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    public void handlePermissionResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == STORAGE_PERMISSION_CODE) {
            handleStoragePermissionsResult();
        } else */
        if (requestCode == DEFAULT_SMS_APP_PERMISSION_CODE) {
            handleDefaultSmsAppPermissionResult(resultCode);
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

    private void handleDefaultSmsAppPermissionResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(activity, "Success requesting ROLE_SMS!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Failed requesting ROLE_SMS!", Toast.LENGTH_SHORT).show();
        }
    }
}
