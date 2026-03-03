package com.jacobs.androidnotifyme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * CustomNotifierListener
 * Intercepts Android OS notifications, extracts package/title/text details,
 * and broadcasts them locally for MainActivity processing.
 */
public class CustomNotifierListener extends NotificationListenerService {

    private static final String TAG = "CustomNotifierListener";
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null || sbn.getNotification().extras == null) {
            return;
        }

        String packageName = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
        
        String titleData = extras.getString("android.title", "");
        CharSequence textSeq = extras.getCharSequence("android.text");
        String textData = textSeq != null ? textSeq.toString() : "";

        Log.d(TAG, "Package: " + packageName);
        Log.d(TAG, "Title: " + titleData);
        Log.d(TAG, "Text: " + textData);

        Intent msgrcv = new Intent("Msg");
        msgrcv.putExtra("package", packageName);
        msgrcv.putExtra("title", titleData);
        msgrcv.putExtra("text", textData);
        
        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Removed");
    }
}
