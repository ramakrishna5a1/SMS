package com.messages.smsmessagesimporter.Activities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.messages.smsmessagesimporter.DataHandler.DataProcessedCallback;
import com.messages.smsmessagesimporter.DataHandler.DataUtils;
import com.messages.smsmessagesimporter.DataHandler.JSONDataParser;
import com.messages.smsmessagesimporter.DataHandler.JSONFileHandler;
import com.messages.smsmessagesimporter.DataHandler.JSONFirebaseHandler;
import com.messages.smsmessagesimporter.DataHandler.SMSManager;
import com.messages.smsmessagesimporter.R;
import com.messages.smsmessagesimporter.Utils.PermissionsHandler;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class DataImporterActivity extends AppCompatActivity implements DataProcessedCallback {
    private final String CLASS_TAG = "DataImporterActivity";
    private static final int REQUEST_CODE_SMS_PERMISSION = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int DEFAULT_SMS_APP_PERMISSION_CODE = 102;
    private static final int NETWORK_PERMISSION = 103;
    private static final int REQUEST_PICK_FILE = 104;

    private JSONFileHandler fileHandler;
    private JSONFirebaseHandler firebaseHandler;
    private DataUtils dataUtils;
    private PermissionsHandler permissionsHandler;

    Button buttonFile = null;
    Button buttonCloud = null;
    TextView permissionsView = null;
    TextView dataSourcePath = null;
    TextView dataParse = null;
    TextView numMessagesWritten = null;
    Button buttonViewMessages = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_message_importer);

        buttonFile = findViewById(R.id.import_messages_file);
        buttonCloud = findViewById(R.id.import_messages_cloud);
        buttonViewMessages = findViewById(R.id.view_messages);

        permissionsView = findViewById(R.id.permissions_acquired);
        dataSourcePath = findViewById(R.id.data_source);
        dataParse = findViewById(R.id.data_parsing);
        numMessagesWritten = findViewById(R.id.num_messages_written);

        fileHandler = new JSONFileHandler(this);
        firebaseHandler = new JSONFirebaseHandler(this);
        dataUtils = DataUtils.getInstance();
        permissionsHandler = new PermissionsHandler(this, storageActivityResultLauncher);
        permissionsHandler.checkAndRequestPermissions();
        permissionsHandler.updatePermissionsText(permissionsView);

        buttonFile.setOnClickListener((View v) -> {
            dataUtils.clearDataUtils();
            fileHandler.pickFile();
            updateInfoText();
        });
        buttonCloud.setOnClickListener((View v) -> {
            dataUtils.clearDataUtils();
            showCustomDialog();
            updateInfoText();
        });
        updateInfoText();
        buttonViewMessages.setOnClickListener((View v) -> {
            if(dataUtils.smsEntityList.size() != 0){
                Intent intent = new Intent(DataImporterActivity.this, SmsListActivity.class);
                startActivity(intent);
            }else {
                Toast.makeText(this, "No messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.auth_dialog, null);
        EditText editTextAuthCode = dialogView.findViewById(R.id.editTextAuthCode);
        TextView cancel = dialogView.findViewById(R.id.auth_cancel);
        TextView ok = dialogView.findViewById(R.id.auth_ok);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        ok.setOnClickListener(v -> {
            String authCode = editTextAuthCode.getText().toString().trim();
            if (!Objects.equals(authCode, "")) {
                CompletableFuture<Boolean> ret = firebaseHandler.authenticateUser(authCode);
                ret.thenAccept(isAuthenticated -> {
                            if (isAuthenticated) {
                                Log.i(CLASS_TAG, "Spread sheet have child: " + authCode);
                                Toast.makeText(this, "User found!", Toast.LENGTH_LONG).show();
                                firebaseHandler.processChild();
                            } else {
                                Log.i(CLASS_TAG, "Spread sheet doesn't have child: " + authCode);
                                Toast.makeText(this, "No user found!", Toast.LENGTH_LONG).show();
                            }
                        })
                        .exceptionally(e -> {
                                    return null;
                                }
                        );
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Enter code..!", Toast.LENGTH_LONG).show();
            }
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEFAULT_SMS_APP_PERMISSION_CODE) {
            permissionsHandler.handleDefaultSmsAppPermissionResult(resultCode);
            permissionsHandler.updatePermissionsText(permissionsView);
        } else if (requestCode == REQUEST_PICK_FILE) {
            if (resultCode != RESULT_OK) {
                dataUtils.setJsonSourcePath("NULL");
                return;
            }
            fileHandler.handleActivityResult(requestCode, resultCode, data);
        }
    }

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            if (Environment.isExternalStorageManager()) {
                //Manage External Storage Permissions Granted
                Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
            } else {
                Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            //Below android 11
        }
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("DataImporterActivity", "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_SMS_PERMISSION:
                permissionsHandler.handleSmsPermissionsResult(grantResults);
                break;
            case STORAGE_PERMISSION_CODE:
                permissionsHandler.handleStoragePermissionsResult(grantResults);
                break;
            case NETWORK_PERMISSION:
                permissionsHandler.handleNetworkPermissionsResult(grantResults);
                break;
            default:
                Log.i("Request Permission Result Invalid", "Code: " + requestCode);
                break;
        }
        permissionsHandler.updatePermissionsText(permissionsView);
    }

    @Override
    public void onJSONSourceSelected() {
        // Source selected so start reading data
    }

    @Override
    public void onJSONDataReadDone() {
        /*
         * JSON Data read done so start parsing the data
         * */
        Log.i(CLASS_TAG, "onJSONDataReadDone");
        Log.i(CLASS_TAG, dataUtils.getJsonString());
        if (!Objects.equals(dataUtils.getJsonString(), "NULL")) {
            new Thread(new JSONDataParser(this)).start();
        }
    }

    @Override
    public void onJSONDataProcessed() {
        /*
         * JSON data parsed so start importing messages
         * */
        Log.i(CLASS_TAG, "onJSONDataProcessed");
        if (!dataUtils.smsEntityList.isEmpty()) {
            new Thread(new SMSManager(this)).start();
        }
    }

    @Override
    public void onMessagesImported() {
        updateInfoText();
    }

    private void updateInfoText() {
        dataSourcePath.setText(getResources().getString(R.string.data_source) + "\n" + dataUtils.getJsonSourcePath());
        String dataFetchSuccessOrFailure = dataUtils.getJsonParseSuccess() ? "Success" : "Failure";
        dataParse.setText(getResources().getString(R.string.file_parsing) + " " + dataFetchSuccessOrFailure);
        numMessagesWritten.setText(getResources().getString(R.string.number_of_messages_written) + " " + dataUtils.insertedMessageIndexes.size() + "/" + dataUtils.smsEntityList.size());
        buttonViewMessages.setEnabled(!dataUtils.smsEntityList.isEmpty());
    }
}