package com.example.cryonix.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.VillageAlert;
import com.google.android.material.button.MaterialButton;

/** A13 One alert of my village: level, full message, time and whether it is still active. */
public class AlertDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ALERT_ID = "alert_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.RECEIVE_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_alert_detail);
        TopBarHelper.setup(this, R.string.alert_detail_title);

        VillagePack pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }
        VillageAlert alert = pack.findAlert(getIntent().getIntExtra(EXTRA_ALERT_ID, -1));
        if (alert == null) {
            Toast.makeText(this, R.string.alert_detail_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView textAlertDetailLevel = findViewById(R.id.textAlertDetailLevel);
        TextView textAlertDetailMessage = findViewById(R.id.textAlertDetailMessage);
        TextView textAlertDetailLake = findViewById(R.id.textAlertDetailLake);
        TextView textAlertDetailTime = findViewById(R.id.textAlertDetailTime);
        TextView textAlertDetailStatus = findViewById(R.id.textAlertDetailStatus);
        MaterialButton buttonAlertDetailRoute = findViewById(R.id.buttonAlertDetailRoute);

        LevelBadgeHelper.show(textAlertDetailLevel, alert.getLevel());
        textAlertDetailMessage.setText(alert.getMessage());
        textAlertDetailLake.setText(getString(R.string.alert_detail_lake, alert.getLakeName()));
        textAlertDetailTime.setText(getString(R.string.alert_detail_time, alert.getTime()));
        textAlertDetailStatus.setText(alert.isActive() ? R.string.history_status_active : R.string.history_status_ended);

        // Route button only while a Watch or Warning is active
        if (alert.isActive() && alert.getLevel().isSerious()) {
            buttonAlertDetailRoute.setVisibility(View.VISIBLE);
            buttonAlertDetailRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(AlertDetailActivity.this, RouteActivity.class));
                }
            });
        }
    }
}
