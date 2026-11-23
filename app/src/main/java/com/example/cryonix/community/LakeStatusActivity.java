package com.example.cryonix.community;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.AlertLevel;

/**
 * A18 Lake above my village (CA-08): level and last check in simple words.
 * No area, model numbers or other technical data for community members.
 */
public class LakeStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.RECEIVE_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_lake_status);
        TopBarHelper.setup(this, R.string.lake_status_title);

        VillagePack pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }

        TextView textLakeStatusName = findViewById(R.id.textLakeStatusName);
        TextView textLakeStatusLevel = findViewById(R.id.textLakeStatusLevel);
        TextView textLakeStatusText = findViewById(R.id.textLakeStatusText);
        TextView textLakeStatusChecked = findViewById(R.id.textLakeStatusChecked);

        if (pack.lakeName.isEmpty()) {
            textLakeStatusName.setText(R.string.lake_status_none);
            textLakeStatusLevel.setVisibility(View.GONE);
            textLakeStatusText.setVisibility(View.GONE);
            textLakeStatusChecked.setVisibility(View.GONE);
            return;
        }

        textLakeStatusName.setText(pack.lakeName);
        LevelBadgeHelper.show(textLakeStatusLevel, pack.lakeLevel);
        textLakeStatusText.setText(getSimpleText(pack.lakeLevel));
        textLakeStatusChecked.setText(getString(R.string.lake_status_checked, pack.lakeCheckedOn));
    }

    private int getSimpleText(AlertLevel level) {
        if (level == AlertLevel.WARNING) return R.string.lake_status_warning;
        if (level == AlertLevel.WATCH) return R.string.lake_status_watch;
        if (level == AlertLevel.ADVISORY) return R.string.lake_status_advisory;
        return R.string.lake_status_baseline;
    }
}
