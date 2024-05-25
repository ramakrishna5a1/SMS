// src/main/java/com/messages/smsmessagesimporter/Activities/SmsListActivity.java
package com.messages.smsmessagesimporter.Activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.messages.smsmessagesimporter.DataHandler.DataUtils;
import com.messages.smsmessagesimporter.R;
import com.messages.smsmessagesimporter.Utils.SmsAdapter;
import com.messages.smsmessagesimporter.Utils.SmsEntity;

import java.util.List;

public class SmsListActivity extends AppCompatActivity {

    private ListView smsListView;
    private SmsAdapter smsAdapter;
    private List<SmsEntity> smsList;
    private DataUtils dataUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_list);
        // Setup toolbar as ActionBar

        smsListView = findViewById(R.id.smsListView);
        dataUtils = DataUtils.getInstance();

        smsAdapter = new SmsAdapter(this, dataUtils.smsEntityList);
        smsListView.setAdapter(smsAdapter);
    }
}
