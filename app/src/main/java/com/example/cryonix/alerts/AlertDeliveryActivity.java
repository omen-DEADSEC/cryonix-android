package com.example.cryonix.alerts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.Alert;
import com.example.cryonix.data.model.AlertLevel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;

/**
 * A28 Alert delivery (DR-02). All staff of the valley can watch it;
 * only roles with CANCEL_ALERTS see "Cancel alert".
 * The screen refreshes every second while the demo numbers are still changing.
 */
public class AlertDeliveryActivity extends AppCompatActivity {

    public static final String EXTRA_ALERT_ID = "alert_id";

    private static final long REFRESH_MILLIS = 1000;

    private Alert alert;
    private UserRole currentRole;

    private TextView textDeliveryStatus;
    private TextView textDeliveryCancelNote;
    private LinearLayout layoutDeliveryChannels;
    private TextView textDeliveryVillagesTitle;
    private LinearLayout layoutDeliveryVillages;
    private MaterialButton buttonDeliveryCancel;
    private MaterialButton buttonDeliveryAllClear;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            showDelivery();
            // Keep refreshing until the numbers stop changing (a little after the 2-minute limit)
            if (alert.isActive() && alert.getSecondsSinceSent() <= Alert.CONFIRM_LIMIT_SECONDS + 2) {
                refreshHandler.postDelayed(this, REFRESH_MILLIS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_ALERTS)) {
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        currentRole = sessionManager.getUserRole();
        alert = findVisibleAlert(getIntent().getIntExtra(EXTRA_ALERT_ID, -1), sessionManager);
        if (alert == null) {
            Toast.makeText(this, R.string.delivery_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_alert_delivery);
        TopBarHelper.setup(this, R.string.delivery_title);

        TextView textDeliveryLevel = findViewById(R.id.textDeliveryLevel);
        TextView textDeliveryLake = findViewById(R.id.textDeliveryLake);
        TextView textDeliverySentBy = findViewById(R.id.textDeliverySentBy);
        TextView textDeliveryMessage = findViewById(R.id.textDeliveryMessage);
        textDeliveryStatus = findViewById(R.id.textDeliveryStatus);
        textDeliveryCancelNote = findViewById(R.id.textDeliveryCancelNote);
        layoutDeliveryChannels = findViewById(R.id.layoutDeliveryChannels);
        textDeliveryVillagesTitle = findViewById(R.id.textDeliveryVillagesTitle);
        layoutDeliveryVillages = findViewById(R.id.layoutDeliveryVillages);
        buttonDeliveryCancel = findViewById(R.id.buttonDeliveryCancel);
        buttonDeliveryAllClear = findViewById(R.id.buttonDeliveryAllClear);
        buttonDeliveryAllClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askAllClear();
            }
        });

        LevelBadgeHelper.show(textDeliveryLevel, alert.getLevel());
        textDeliveryLake.setText(alert.getLakeName());
        textDeliverySentBy.setText(getString(R.string.delivery_sent_by, alert.getTimeText(), alert.getCreatedBy()));
        textDeliveryMessage.setText(alert.getMessage());

        buttonDeliveryCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCancelReason();
            }
        });
    }

    /** Organisation staff may only open alerts of their own valleys. */
    private Alert findVisibleAlert(int alertId, SessionManager sessionManager) {
        List<Alert> alertList = FakeDatabase.getAlertsFor(sessionManager.getUserRole(), sessionManager.getUserEmail());
        for (Alert oneAlert : alertList) {
            if (oneAlert.getId() == alertId) {
                return oneAlert;
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alert != null && layoutDeliveryChannels != null) {
            refreshHandler.post(refreshRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void showDelivery() {
        AlertsListActivity.showStatus(textDeliveryStatus, alert.isActive());

        if (!alert.isActive()) {
            // AL-05: the first alert is closed with time and user
            textDeliveryCancelNote.setText(getString(R.string.delivery_closed_note, alert.getClosedTime(),
                    alert.getClosedBy(), alert.getCancelReason()));
            textDeliveryCancelNote.setVisibility(View.VISIBLE);
        }
        boolean canCancel = alert.isActive() && currentRole != null && currentRole.can(Permission.CANCEL_ALERTS);
        buttonDeliveryCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        // An All clear itself cannot get another All clear
        boolean canAllClear = canCancel && alert.getLevel() != AlertLevel.ALL_CLEAR;
        buttonDeliveryAllClear.setVisibility(canAllClear ? View.VISIBLE : View.GONE);

        // Channels
        layoutDeliveryChannels.removeAllViews();
        for (String channel : alert.getChannels()) {
            View channelView = LayoutInflater.from(this).inflate(R.layout.item_channel_status, layoutDeliveryChannels, false);
            TextView textChannelName = channelView.findViewById(R.id.textChannelName);
            ProgressBar progressChannel = channelView.findViewById(R.id.progressChannel);
            TextView textChannelNumbers = channelView.findViewById(R.id.textChannelNumbers);

            int total = alert.getChannelTotal(channel);
            int sent = alert.getChannelSent(channel);
            int delivered = alert.getChannelDelivered(channel);

            textChannelName.setText(channel);
            progressChannel.setProgress(total == 0 ? 0 : delivered * 100 / total);
            textChannelNumbers.setText(getString(R.string.delivery_channel_numbers, sent, total, delivered));
            layoutDeliveryChannels.addView(channelView);
        }

        // Villages
        List<String> villageNames = alert.getVillages();
        textDeliveryVillagesTitle.setText(getString(R.string.delivery_villages,
                alert.getConfirmedVillageCount(), villageNames.size()));
        layoutDeliveryVillages.removeAllViews();
        boolean isPastLimit = alert.getSecondsSinceSent() > Alert.CONFIRM_LIMIT_SECONDS;

        for (int i = 0; i < villageNames.size(); i++) {
            View villageView = LayoutInflater.from(this).inflate(R.layout.item_village_confirm, layoutDeliveryVillages, false);
            TextView textVillageConfirmName = villageView.findViewById(R.id.textVillageConfirmName);
            TextView textVillageConfirmStatus = villageView.findViewById(R.id.textVillageConfirmStatus);
            textVillageConfirmName.setText(villageNames.get(i));

            if (alert.isVillageConfirmed(i)) {
                textVillageConfirmStatus.setText(R.string.delivery_confirmed);
                textVillageConfirmStatus.setTextColor(ContextCompat.getColor(this, R.color.color_success));
            } else if (isPastLimit) {
                textVillageConfirmStatus.setText(R.string.delivery_not_confirmed);
                textVillageConfirmStatus.setTextColor(ContextCompat.getColor(this, R.color.color_error));
                villageView.setBackgroundResource(R.drawable.bg_message_error);
            } else {
                textVillageConfirmStatus.setText(R.string.delivery_waiting);
                textVillageConfirmStatus.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));
            }
            layoutDeliveryVillages.addView(villageView);
        }
    }

    private void askCancelReason() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_alert, null);
        final TextInputLayout inputLayoutCancelReason = dialogView.findViewById(R.id.inputLayoutCancelReason);
        final TextInputEditText editCancelReason = dialogView.findViewById(R.id.editCancelReason);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delivery_cancel)
                .setView(dialogView)
                .setPositiveButton(R.string.delivery_cancel, null)
                .setNegativeButton(R.string.button_back, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reason = editCancelReason.getText() == null ? "" : editCancelReason.getText().toString().trim();
                if (reason.isEmpty()) {
                    inputLayoutCancelReason.setError(getString(R.string.delivery_cancel_error));
                    return;
                }
                String userName = new SessionManager(AlertDeliveryActivity.this).getUserName();
                FakeDatabase.cancelAlert(alert, reason, userName);
                Toast.makeText(AlertDeliveryActivity.this, R.string.delivery_cancelled, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showDelivery();
            }
        });
    }

    /** AL-05: preview, confirm, then send the All clear and open its delivery screen. */
    private void askAllClear() {
        final String message = getString(R.string.alert_template_all_clear, alert.getLakeName());
        String preview = getString(R.string.delivery_all_clear_preview, alert.getVillages().size(),
                String.format(Locale.ENGLISH, "%,d", FakeDatabase.countPeople(alert.getVillages())),
                alert.getChannels().size(), message);

        new AlertDialog.Builder(this)
                .setTitle(R.string.delivery_all_clear)
                .setMessage(preview)
                .setPositiveButton(R.string.create_alert_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userName = new SessionManager(AlertDeliveryActivity.this).getUserName();
                        Alert allClear = FakeDatabase.sendAllClear(alert, message, userName);
                        Toast.makeText(AlertDeliveryActivity.this, R.string.delivery_all_clear_sent, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(AlertDeliveryActivity.this, AlertDeliveryActivity.class);
                        intent.putExtra(EXTRA_ALERT_ID, allClear.getId());
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}
