package com.example.cryonix.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cryonix.R;
import com.example.cryonix.community.FullScreenAlertActivity;
import com.example.cryonix.data.model.VillageAlert;

/**
 * Community alert: urgent notification + full-screen alert (CA-03).
 * DEMO: there is no push server yet, so the home screen calls showIfNew() when it finds an
 * alert the user has not seen. Later Firebase Cloud Messaging will call the same code.
 */
public class CommunityAlertHelper {

    public static final String CHANNEL_COMMUNITY_ALERTS = "community_alerts";
    private static final int NOTIFICATION_ID_ALERT = 3001;

    private static final String PREFS_NAME = "cryonix_community";
    private static final String KEY_LAST_SEEN_ALERT = "last_seen_alert_id";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_COMMUNITY_ALERTS,
                    context.getString(R.string.community_channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.community_channel_text));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 800, 400, 800, 400, 800});
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /** Shows the alert once. Returns true if it was new. */
    public static boolean showIfNew(Context context, VillageAlert alert, String safePointName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (alert.getId() <= prefs.getInt(KEY_LAST_SEEN_ALERT, 0)) {
            return false;
        }
        prefs.edit().putInt(KEY_LAST_SEEN_ALERT, alert.getId()).apply();
        showNotification(context, alert, safePointName);
        context.startActivity(makeFullScreenIntent(context, alert.getId(), false));
        return true;
    }

    public static Intent makeFullScreenIntent(Context context, int alertId, boolean isPractice) {
        Intent intent = new Intent(context, FullScreenAlertActivity.class);
        intent.putExtra(FullScreenAlertActivity.EXTRA_ALERT_ID, alertId);
        intent.putExtra(FullScreenAlertActivity.EXTRA_PRACTICE, isPractice);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static void showNotification(Context context, VillageAlert alert, String safePointName) {
        if (!NotificationHelper.canPostNotifications(context)) {
            return;
        }
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, alert.getId(),
                makeFullScreenIntent(context, alert.getId(), false),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = context.getString(R.string.community_notification_title, alert.getLevel().getLabel(), alert.getLakeName());
        String text = safePointName == null ? alert.getMessage()
                : context.getString(R.string.community_notification_text, safePointName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_COMMUNITY_ALERTS)
                .setSmallIcon(R.drawable.ic_alert_bell)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(alert.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(fullScreenPendingIntent)
                // Android may show this as a full-screen alert when the phone is locked
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, builder.build());
        } catch (SecurityException e) {
            // Permission removed; the full-screen screen still opens from the app
        }
    }
}
