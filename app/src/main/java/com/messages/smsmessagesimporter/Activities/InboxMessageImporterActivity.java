package com.messages.smsmessagesimporter.Activities;

import static android.app.role.RoleManager.ROLE_SMS;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.messages.smsmessagesimporter.DataHandler.FileHandler;
import com.messages.smsmessagesimporter.DataHandler.JSONFileProperties;
import com.messages.smsmessagesimporter.DataHandler.JsonSMSDataHandler;
import com.messages.smsmessagesimporter.R;

import java.util.Objects;


public class InboxMessageImporterActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SMS_PERMISSION = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private static final int DEFAULT_SMS_APP_PERMISSION_CODE = 102;
    private static final int REQUEST_PICK_FILE = 123;

    private final String[] smsPermissions = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS};

    private FileHandler fileHandler;

    TextView fileText = null;
    TextView permissionsView = null;
    int numOfAcquiredPermissions = 0;
    Button button1 = null;
    private ActivityResultLauncher<Intent> intentLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_message_importer);
        fileHandler = new FileHandler(this);
        button1 = findViewById(R.id.import_messages);
        //button1.setEnabled(false);
        fileText = findViewById(R.id.file_id);
        permissionsView = findViewById(R.id.permissions_acquired);

        // Check and request SMS permissions
        if (!checkSmsPermissions()) {
            requestSmsPermissions();
        }
        if (!checkStoragePermissions()) {
            requestForStoragePermissions();
        }
        prepareIntentLauncher();
        askDefaultSmsHandlerPermission();
        UpdatePermissionsText();

        button1.setOnClickListener(v -> fileHandler.pickFile());
    }

    private void UpdatePermissionsText() {
        String permissionsGranted = "";
        boolean[] allPermissionsGranted = new boolean[3];
        allPermissionsGranted[0] = checkSmsPermissions();
        allPermissionsGranted[1] = checkStoragePermissions();
        allPermissionsGranted[2] = checkDefaultSmsAppPermissions();

        if(allPermissionsGranted[0] && allPermissionsGranted[1] && allPermissionsGranted[2]){
            permissionsGranted = permissionsGranted.concat("All permissions are granted !");
            permissionsView.setTextColor(Color.BLUE);
        }else{
            permissionsGranted = permissionsGranted.concat(String.format("SMS Permission %s\n", (checkSmsPermissions() ? "Granted" : "Not granted")));
            permissionsGranted = permissionsGranted.concat(String.format("Storage Permission %s\n", (checkStoragePermissions() ? "Granted" : "Not granted")));
            permissionsGranted = permissionsGranted.concat(String.format("Default SMS App Permission %s\n\n", (checkDefaultSmsAppPermissions() ? "Granted" : "Not granted")));
            permissionsGranted = permissionsGranted.concat("Note: Please close and open App Again.!!");
            permissionsView.setTextColor(Color.RED);
        }
        permissionsView.setText(permissionsGranted);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEFAULT_SMS_APP_PERMISSION_CODE) {
            if (resultCode == RESULT_OK) {
                numOfAcquiredPermissions++;
            }
            UpdatePermissionsText();
        } else if (requestCode == REQUEST_PICK_FILE) {
            JSONFileProperties jsonFileProperties = new JSONFileProperties();
            if (resultCode != RESULT_OK) {
                jsonFileProperties.setJsonFilePath("Unable to select file");
                return;
            }
            fileHandler.handleActivityResult(requestCode, resultCode, data, jsonFileProperties);
            Log.i("selectedFilePath: ", jsonFileProperties.getJsonFilePath());

            if (!Objects.equals(jsonFileProperties.getJsonFilePath(), "No file selected !")) {
                fileText.setText(jsonFileProperties.getJsonFilePath());
                new Thread(new JsonSMSDataHandler(this, jsonFileProperties)).start();
            } else {
                fileText.setText(jsonFileProperties.getJsonFilePath());
            }
        }
    }

    private boolean checkDefaultSmsAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = this.getSystemService(RoleManager.class);
            return roleManager.isRoleHeld(RoleManager.ROLE_SMS);
        } else {
            // For versions before Android Q, check if the app is the default SMS app by package name
            PackageManager packageManager = this.getPackageManager();
            return this.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(this));
        }
    }

    private boolean checkSmsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int sendSmsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
            int readSmsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
            return sendSmsPermission == PackageManager.PERMISSION_GRANTED && readSmsPermission == PackageManager.PERMISSION_GRANTED;
        } else {
            // Permissions are granted automatically on versions below M
            return true;
        }
    }

    private void requestSmsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, smsPermissions, REQUEST_CODE_SMS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_SMS_PERMISSION:
                handleSmsPermissionsResult(grantResults);
                break;
            case STORAGE_PERMISSION_CODE:
                handleStoragePermissionsResult(grantResults);
                break;
        }
        UpdatePermissionsText();
    }

    private void handleSmsPermissionsResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // SMS permissions granted
            Toast.makeText(this, "SMS permissions granted", Toast.LENGTH_SHORT).show();
        } else {
            // SMS permissions denied
            Toast.makeText(this, "SMS permissions denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleStoragePermissionsResult(int[] grantResults) {
        if (grantResults.length > 0) {
            boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (read && write) {
                Toast.makeText(InboxMessageImporterActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(InboxMessageImporterActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void prepareIntentLauncher() {
        intentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(InboxMessageImporterActivity.this, "Success requesting ROLE_SMS!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(InboxMessageImporterActivity.this, "Failed requesting ROLE_SMS!", Toast.LENGTH_SHORT).show();
            }
            UpdatePermissionsText();
        });
    }

    private void askDefaultSmsHandlerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            String role = ROLE_SMS;
            // Check if the app has permission to be the default SMS app
            boolean isRoleAvailable = roleManager.isRoleAvailable(role);
            if (isRoleAvailable) {
                // Check whether your app is already holding the default SMS app role.
                boolean isRoleHeld = roleManager.isRoleHeld(role);
                if (!isRoleHeld) {
                    intentLauncher.launch(roleManager.createRequestRoleIntent(role));
                } else {
                    // Request permission for SMS
                    numOfAcquiredPermissions++;
                    UpdatePermissionsText();
                }
            }
        } else {
            Log.i("DefaultApp:", "Build version is less than Q");
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, DEFAULT_SMS_APP_PERMISSION_CODE);
        }
    }

    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
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
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //Android is 11 (R) or above
                    if (Environment.isExternalStorageManager()) {
                        //Manage External Storage Permissions Granted
                        Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                    } else {
                        Toast.makeText(InboxMessageImporterActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Below android 11
                }
            });
}
