package com.example.cryonix.common;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.notifications.NotificationsActivity;

/**
 * Phone notifications for staff (DR-04).
 * DEMO: shows a local notification. Later the backend sends it with Firebase Cloud Messaging.
 */
public class NotificationHelper {

    public static final String CHANNEL_STAFF_NOTICES = "staff_notices";
    private static final int NOTIFICATION_ID_SUMMARY = 2001;

    /** Android 8+ needs a channel before any notification can show. */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_STAFF_NOTICES,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_channel_text));
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static boolean canPostNotifications(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /** "You have 3 unread notices". Tapping it opens the Notifications screen. */
    public static void showUnreadSummary(Context context, int unreadCount, String firstTitle) {
        if (unreadCount <= 0 || !canPostNotifications(context)) {
            return;
        }

        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STAFF_NOTICES)
                .setSmallIcon(R.drawable.ic_alert_bell)
                .setContentTitle(context.getString(R.string.notification_unread_title, unreadCount))
                .setContentText(firstTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SUMMARY, builder.build());
        } catch (SecurityException e) {
            // Permission was taken away while the app was open; nothing to show
        }
    }
}
