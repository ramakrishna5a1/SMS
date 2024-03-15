package com.messages.smsmessagesimporter.Services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void showSnackBar(View view, String message)
    {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getTimeStamp(String timestamp) {
        if (!TextUtils.isEmpty(timestamp)) {
            long datetime = Long.parseLong(timestamp);
            Date date = new Date(datetime);
            DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
            return formatter.format(date);
        } else {
            return "--";
        }
    }

    public static void driver() {
        String dateString = "13/03/2024 15:45"; // Example date string
        try {
            long millis = dateToLong(dateString);
            System.out.println("Milliseconds since epoch: " + millis);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static long dateToLong(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = sdf.parse(dateString);
        return date.getTime();
    }
}
