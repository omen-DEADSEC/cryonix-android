package com.example.cryonix.community;

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
import com.example.cryonix.common.CommunityBottomBarHelper;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.VillageAlert;

/** A12 Alerts for my village (CA-10): time, level and message, newest first. All clear stays in the list (CA-07). */
public class AlertsHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.RECEIVE_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_alerts_history);
        TopBarHelper.setupWithoutBack(this, R.string.history_title);
        CommunityBottomBarHelper.setup(this, CommunityBottomBarHelper.TAB_ALERTS);

        VillagePack pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }

        LinearLayout layoutHistoryList = findViewById(R.id.layoutHistoryList);
        TextView textHistoryEmpty = findViewById(R.id.textHistoryEmpty);
        textHistoryEmpty.setVisibility(pack.alerts.isEmpty() ? View.VISIBLE : View.GONE);

        for (final VillageAlert alert : pack.alerts) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_alert_row, layoutHistoryList, false);
            TextView textAlertRowLevel = rowView.findViewById(R.id.textAlertRowLevel);
            TextView textAlertRowStatus = rowView.findViewById(R.id.textAlertRowStatus);
            TextView textAlertRowLake = rowView.findViewById(R.id.textAlertRowLake);
            TextView textAlertRowDetails = rowView.findViewById(R.id.textAlertRowDetails);

            LevelBadgeHelper.show(textAlertRowLevel, alert.getLevel());
            textAlertRowStatus.setText(alert.isActive() ? R.string.history_status_active : R.string.history_status_ended);
            textAlertRowStatus.setTextColor(ContextCompat.getColor(this,
                    alert.isActive() ? R.color.color_success : R.color.color_text_secondary));
            textAlertRowStatus.setBackgroundResource(alert.isActive() ? R.drawable.bg_message_success : R.drawable.bg_chip);
            textAlertRowLake.setText(alert.getMessage());
            textAlertRowLake.setTextSize(14);
            textAlertRowDetails.setText(alert.getTime());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AlertsHistoryActivity.this, AlertDetailActivity.class);
                    intent.putExtra(AlertDetailActivity.EXTRA_ALERT_ID, alert.getId());
                    startActivity(intent);
                }
            });
            layoutHistoryList.addView(rowView);
        }
    }
}
