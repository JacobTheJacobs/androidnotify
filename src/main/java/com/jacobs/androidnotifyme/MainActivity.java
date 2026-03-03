package com.jacobs.androidnotifyme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * MainActivity
 * Monitors specific broadcast events from CustomNotifierListener
 * and stores target notifications in Firebase Firestore.
 */
public class MainActivity extends AppCompatActivity {
    
    private TableLayout tab;
    private FirebaseFirestore db;
    private static final String TARGET_PACKAGE_1 = "com.zyncas.signals";
    private static final String TARGET_PACKAGE_2 = "com.jacobs.androidnotifyme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tab = findViewById(R.id.tab);
        db = FirebaseFirestore.getInstance();

        requestNotificationAccess();

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
    }

    private void requestNotificationAccess() {
        NotificationManagerCompat n = NotificationManagerCompat.from(getApplicationContext());
        if (!n.areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getStringExtra("package");
            String titleData = intent.getStringExtra("title");
            String textData = intent.getStringExtra("text");

            if (packageName != null && (packageName.equals(TARGET_PACKAGE_1) || packageName.equals(TARGET_PACKAGE_2))) {
                storeNotificationInFirestore(packageName, titleData, textData);
            }
            displayNotificationInTable(packageName, titleData, textData);
        }
    };

    private void storeNotificationInFirestore(String packageName, String title, String text) {
        long time = System.currentTimeMillis();
        Map<String, String> notif = new HashMap<>();
        notif.put("package", packageName);
        notif.put("title", title != null ? title : "");
        notif.put("text", text != null ? text : "");
        notif.put("time", String.valueOf(time));
        
        db.collection("notifications").document(String.valueOf(time)).set(notif);
    }

    private void displayNotificationInTable(String packageName, String title, String text) {
        TableRow tr = new TableRow(getApplicationContext());
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        
        TextView textview = new TextView(getApplicationContext());
        textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        textview.setTextSize(20);
        textview.setTextColor(Color.parseColor("#0B0719"));
        textview.setText(Html.fromHtml("<b>" + packageName + "</b><br><i>" + title + "</i> : " + text));
        
        tr.addView(textview);
        tab.addView(tr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }
}
