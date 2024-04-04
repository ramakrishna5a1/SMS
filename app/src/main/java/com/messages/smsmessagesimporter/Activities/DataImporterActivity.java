package com.messages.smsmessagesimporter.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.messages.smsmessagesimporter.DataHandler.DataProcessedCallback;
import com.messages.smsmessagesimporter.DataHandler.JSONDataParser;
import com.messages.smsmessagesimporter.DataHandler.JSONDataUtils;
import com.messages.smsmessagesimporter.DataHandler.JSONFileHandler;
import com.messages.smsmessagesimporter.DataHandler.JSONFirebaseHandler;
import com.messages.smsmessagesimporter.DataHandler.PermissionsHandler;
import com.messages.smsmessagesimporter.DataHandler.SMSManager;
import com.messages.smsmessagesimporter.R;

import java.util.Objects;


public class DataImporterActivity extends AppCompatActivity implements DataProcessedCallback {

    private static final int REQUEST_PICK_FILE = 123;
    private JSONFileHandler fileHandler;
    private JSONFirebaseHandler firebaseHandler;
    private JSONDataUtils jsonDataUtils;
    private PermissionsHandler permissionsHandler;

    TextView fileText = null;
    TextView permissionsView = null;
    Button buttonFile = null;
    Button buttonCloud = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_message_importer);
        fileHandler = new JSONFileHandler(this);
        firebaseHandler = new JSONFirebaseHandler(this);
        jsonDataUtils = JSONDataUtils.getInstance();

        buttonFile = findViewById(R.id.import_messages_file);
        buttonCloud = findViewById(R.id.import_messages_cloud);
        fileText = findViewById(R.id.file_id);
        permissionsView = findViewById(R.id.permissions_acquired);

        buttonFile.setOnClickListener((View v) -> {
            fileHandler.pickFile();
        });
        buttonCloud.setOnClickListener((View v) -> {
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE) {
            if (resultCode != RESULT_OK) {
                jsonDataUtils.setJsonFilePath("NULL");
                return;
            }
            fileHandler.handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onJSONDataReadDone() {
        Log.i("File Activity", "onReadFileDone");
        startMessageProcessing();
    }

    @Override
    public void onJSONDataProcessed() {
        Log.i("File Activity", "onJSONDataProcessed");
        if (!jsonDataUtils.getSmsEntityList().isEmpty()) {
            new Thread(new SMSManager(this)).start();
        }
    }

    void startMessageProcessing() {
        if (!Objects.equals(jsonDataUtils.getJsonFilePath(), "NULL")) {
            new Thread(new JSONDataParser(this)).start();
        }
    }
}