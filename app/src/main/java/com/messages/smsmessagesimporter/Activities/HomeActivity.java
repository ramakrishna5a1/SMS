package com.messages.smsmessagesimporter.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.messages.smsmessagesimporter.DataHandler.PermissionsHandler;
import com.messages.smsmessagesimporter.R;

public class HomeActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SMS_PERMISSION = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int DEFAULT_SMS_APP_PERMISSION_CODE = 102;

    private PermissionsHandler permissionsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        permissionsHandler = new PermissionsHandler(this, storageActivityResultLauncher);
        //permissionsHandler.checkAndRequestPermissions();

        Intent intent = new Intent(this, DataImporterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEFAULT_SMS_APP_PERMISSION_CODE) {
            permissionsHandler.handlePermissionResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_SMS_PERMISSION:
                permissionsHandler.handleSmsPermissionsResult(grantResults);
                break;
            case STORAGE_PERMISSION_CODE:
                permissionsHandler.handleStoragePermissionsResult(grantResults);
                break;
        }
    }

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            if (Environment.isExternalStorageManager()) {
                //Manage External Storage Permissions Granted
                Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
            } else {
                Toast.makeText(HomeActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            //Below android 11
        }
    });

    @Override
    protected void onRestart() {
        super.onRestart();
        permissionsHandler.checkAndRequestPermissions();
    }
}