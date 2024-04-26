package com.messages.smsmessagesimporter.DataHandler;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JSONFirebaseHandler extends JSONDataSource {
    private final String CLASS_TAG = "JSONFirebaseHandler";
    DatabaseReference spreadSheetReference;
    private String childSheet;

    public JSONFirebaseHandler(Activity activity) {
        super(activity);
        spreadSheetReference = FirebaseDatabase.getInstance().getReference().child("188QW_O4u37PvjObCHr5UHbBJQgjBjPjwr73uwbDG5A4");
    }

    public CompletableFuture<Boolean> authenticateUser(String authCode) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        spreadSheetReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean childSheetFound = snapshot.hasChild(authCode);
                future.complete(childSheetFound);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }

    public void processChild() {
        try {
            readJsonStringFromSource();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void readJsonStringFromSource() {
        Task<DataSnapshot> dataSnapshotTask = spreadSheetReference
                .child(childSheet)
                .get();

        dataSnapshotTask.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("[");
                        //Process all the messages under given sheet
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            HashMap<String, Object> data = (HashMap<String, Object>) snapshot.getValue();
                            if (data != null) {
                                stringBuilder.append("{");
                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    String key = entry.getKey();
                                    Object value = entry.getValue();
                                    stringBuilder.append("  \"").append(key).append("\": \"").append(value).append("\",");
                                }
                                // Remove the trailing comma and newline
                                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                stringBuilder.append("},");
                            }
                        }
                        // Remove the trailing comma and newline
                        if (stringBuilder.length() > 2) {
                            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
                        }
                        stringBuilder.append("]");

                        String result = stringBuilder.toString();
                        dataUtils.setJsonString(result);
                        callback.onJSONDataReadDone();
                    }
                } else {
                    Log.e("FirebaseError", "Error: " + task.getException().getMessage());
                }
            }
        });
    }
}