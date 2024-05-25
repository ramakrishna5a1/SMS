package com.messages.smsmessagesimporter.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.messages.smsmessagesimporter.R;

import java.util.List;

public class SmsAdapter extends BaseAdapter {
    private Context context;
    private List<SmsEntity> smsList;

    public SmsAdapter(Context context, List<SmsEntity> smsList) {
        this.context = context;
        this.smsList = smsList;
    }

    @Override
    public int getCount() {
        return smsList.size();
    }

    @Override
    public Object getItem(int position) {
        return smsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_sms, parent, false);
        }

        SmsEntity sms = (SmsEntity) getItem(position);

        TextView addressTextView = convertView.findViewById(R.id.addressTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView bodyTextView = convertView.findViewById(R.id.bodyTextView);

        addressTextView.setText(sms.getAddress());
        dateTextView.setText(sms.getDateString());
        bodyTextView.setText(sms.getBody());

        return convertView;
    }
}
