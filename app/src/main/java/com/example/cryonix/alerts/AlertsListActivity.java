package com.example.cryonix.alerts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.StaffBottomBarHelper;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.Alert;
import com.google.android.material.button.MaterialButton;

/**
 * Alerts tab (DR-02). All staff can see alerts of their valleys;
 * only roles with CREATE_ALERTS see the "Create alert" button.
 */
public class AlertsListActivity extends AppCompatActivity {

    private LinearLayout layoutAlertsActive;
    private LinearLayout layoutAlertsPast;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_alerts_list);
        TopBarHelper.setupWithoutBack(this, R.string.alerts_title);
        StaffBottomBarHelper.setup(this, StaffBottomBarHelper.TAB_ALERTS);

        sessionManager = new SessionManager(this);
        layoutAlertsActive = findViewById(R.id.layoutAlertsActive);
        layoutAlertsPast = findViewById(R.id.layoutAlertsPast);
        MaterialButton buttonAlertsCreate = findViewById(R.id.buttonAlertsCreate);

        UserRole role = sessionManager.getUserRole();
        if (role != null && role.can(Permission.CREATE_ALERTS)) {
            buttonAlertsCreate.setVisibility(View.VISIBLE);
        }
        buttonAlertsCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AlertsListActivity.this, CreateAlertActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutAlertsActive != null) {
            showAlerts();
        }
    }

    private void showAlerts() {
        layoutAlertsActive.removeAllViews();
        layoutAlertsPast.removeAllViews();

        for (final Alert alert : FakeDatabase.getAlertsFor(sessionManager.getUserRole(), sessionManager.getUserEmail())) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_alert_row, layoutAlertsActive, false);
            TextView textAlertRowLevel = rowView.findViewById(R.id.textAlertRowLevel);
            TextView textAlertRowStatus = rowView.findViewById(R.id.textAlertRowStatus);
            TextView textAlertRowLake = rowView.findViewById(R.id.textAlertRowLake);
            TextView textAlertRowDetails = rowView.findViewById(R.id.textAlertRowDetails);

            LevelBadgeHelper.show(textAlertRowLevel, alert.getLevel());
            showStatus(textAlertRowStatus, alert.isActive());
            textAlertRowLake.setText(alert.getLakeName());
            textAlertRowDetails.setText(getString(R.string.alerts_row_details, alert.getVillages().size(),
                    alert.getTimeText(), alert.getCreatedBy()));

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AlertsListActivity.this, AlertDeliveryActivity.class);
                    intent.putExtra(AlertDeliveryActivity.EXTRA_ALERT_ID, alert.getId());
                    startActivity(intent);
                }
            });

            if (alert.isActive()) {
                layoutAlertsActive.addView(rowView);
            } else {
                layoutAlertsPast.addView(rowView);
            }
        }

        if (layoutAlertsActive.getChildCount() == 0) {
            addEmptyText(layoutAlertsActive, R.string.alerts_none_active);
        }
        if (layoutAlertsPast.getChildCount() == 0) {
            addEmptyText(layoutAlertsPast, R.string.alerts_none_past);
        }
    }

    /** Green "Active" or grey "Cancelled". Used on the delivery screen too. */
    public static void showStatus(TextView textView, boolean isActive) {
        textView.setText(isActive ? R.string.alerts_status_active : R.string.alerts_status_cancelled);
        textView.setTextColor(ContextCompat.getColor(textView.getContext(),
                isActive ? R.color.color_success : R.color.color_text_secondary));
        textView.setBackgroundResource(isActive ? R.drawable.bg_message_success : R.drawable.bg_chip);
    }

    private void addEmptyText(LinearLayout parent, int textId) {
        TextView textEmpty = new TextView(this);
        textEmpty.setText(textId);
        textEmpty.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));
        textEmpty.setPadding(0, Math.round(8 * getResources().getDisplayMetrics().density), 0, 0);
        parent.addView(textEmpty);
    }
}
