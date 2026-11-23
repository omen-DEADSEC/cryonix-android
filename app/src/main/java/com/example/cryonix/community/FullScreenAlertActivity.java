package com.example.cryonix.community;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.data.VillageDataStore;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Shelter;
import com.example.cryonix.data.model.VillageAlert;

/**
 * A14 Full-screen alert (CA-03).
 * - Background in the level colour, level, lake, message, safe point, "Show my route".
 * - Alarm sound and vibration go on until the person taps the screen or a button.
 * - Shows over the lock screen.
 * EXTRA_PRACTICE = true opens a practice alert from the Safety guide.
 */
public class FullScreenAlertActivity extends AppCompatActivity {

    public static final String EXTRA_ALERT_ID = "alert_id";
    public static final String EXTRA_PRACTICE = "practice";

    private static final long[] VIBRATION_PATTERN = {0, 800, 400, 800, 400, 800, 1200};

    private MediaPlayer alarmPlayer;
    private Vibrator vibrator;
    private TextView textFullAlertTapHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOverLockScreen();
        setContentView(R.layout.activity_full_screen_alert);

        boolean isPractice = getIntent().getBooleanExtra(EXTRA_PRACTICE, false);
        int alertId = getIntent().getIntExtra(EXTRA_ALERT_ID, -1);

        // Use the saved pack so the alert screen also works offline
        String villageName = new SessionManager(this).getUserVillage();
        VillagePack pack = VillageDataStore.getPack(this, villageName);

        AlertLevel level = AlertLevel.WARNING;
        String lakeName = getString(R.string.lake_status_title);
        String message = getString(R.string.what_to_do_warning);
        Shelter safePoint = null;

        if (pack != null) {
            safePoint = pack.getNearestShelterWithRoom();
            if (!pack.lakeName.isEmpty()) {
                lakeName = pack.lakeName;
            }
            VillageAlert alert = pack.findAlert(alertId);
            if (alert != null && !isPractice) {
                level = alert.getLevel();
                lakeName = alert.getLakeName();
                message = alert.getMessage();
            }
        }

        ScrollView layoutFullAlertRoot = findViewById(R.id.layoutFullAlertRoot);
        LinearLayout layoutFullAlertContent = findViewById(R.id.layoutFullAlertContent);
        TextView textFullAlertPractice = findViewById(R.id.textFullAlertPractice);
        ImageView imageFullAlertIcon = findViewById(R.id.imageFullAlertIcon);
        TextView textFullAlertLevel = findViewById(R.id.textFullAlertLevel);
        TextView textFullAlertLake = findViewById(R.id.textFullAlertLake);
        TextView textFullAlertMessage = findViewById(R.id.textFullAlertMessage);
        TextView textFullAlertSafePoint = findViewById(R.id.textFullAlertSafePoint);
        Button buttonFullAlertRoute = findViewById(R.id.buttonFullAlertRoute);
        Button buttonFullAlertStop = findViewById(R.id.buttonFullAlertStop);
        textFullAlertTapHint = findViewById(R.id.textFullAlertTapHint);

        // Watch is orange, Warning red. Text is white on both, so the colours stay fixed (no dark theme change).
        int backgroundColor = level == AlertLevel.WATCH ? 0xFFC2410C : 0xFFB91C1C;
        layoutFullAlertRoot.setBackgroundColor(backgroundColor);
        getWindow().setStatusBarColor(backgroundColor);

        textFullAlertPractice.setVisibility(isPractice ? View.VISIBLE : View.GONE);
        imageFullAlertIcon.setImageResource(level.getIconId());
        textFullAlertLevel.setText(level.getLabel());
        textFullAlertLake.setText(lakeName);
        textFullAlertMessage.setText(message);
        if (safePoint != null) {
            textFullAlertSafePoint.setText(safePoint.getName() + "\n" + getString(R.string.minutes_value, safePoint.getWalkingMinutes()));
        }

        View.OnClickListener stopListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAlarm();
            }
        };
        layoutFullAlertContent.setOnClickListener(stopListener);
        buttonFullAlertStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAlarm();
                finish();
            }
        });

        buttonFullAlertRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAlarm();
                startActivity(new Intent(FullScreenAlertActivity.this, RouteActivity.class));
                finish();
            }
        });

        startAlarm();
    }

    @SuppressWarnings("deprecation")
    private void showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @SuppressWarnings("deprecation")
    private void startAlarm() {
        // Alarm sound, looping
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            alarmPlayer = new MediaPlayer();
            alarmPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            alarmPlayer.setDataSource(this, alarmUri);
            alarmPlayer.setLooping(true);
            alarmPlayer.prepare();
            alarmPlayer.start();
        } catch (Exception e) {
            alarmPlayer = null;   // no sound file on this phone; vibration still works
        }

        // Vibration, repeating from index 0 until stopped
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
            } else {
                vibrator.vibrate(VIBRATION_PATTERN, 0);
            }
        }
    }

    private void stopAlarm() {
        if (alarmPlayer != null) {
            try {
                alarmPlayer.stop();
            } catch (IllegalStateException e) {
                // already stopped
            }
            alarmPlayer.release();
            alarmPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        textFullAlertTapHint.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
