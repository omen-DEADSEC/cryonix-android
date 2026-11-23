package com.example.cryonix.shelters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.Shelter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * A26 Shelter management (SH-01, SH-02). Roles with MANAGE_SHELTERS (admin, organisation staff).
 * Live counts refresh every 5 seconds. Colour changes at 80% (orange) and 100% (red).
 */
public class ShelterManageActivity extends AppCompatActivity {

    private static final long REFRESH_MILLIS = 5000;

    private LinearLayout layoutManageList;
    private SessionManager sessionManager;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            List<Shelter> shelterList = getShelters();
            FakeDatabase.readNewCounts(shelterList);
            showShelters(shelterList);
            refreshHandler.postDelayed(this, REFRESH_MILLIS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.MANAGE_SHELTERS)) {
            return;
        }
        setContentView(R.layout.activity_shelter_manage);
        TopBarHelper.setup(this, R.string.shelter_manage_title);

        sessionManager = new SessionManager(this);
        layoutManageList = findViewById(R.id.layoutManageList);
        MaterialButton buttonManageAdd = findViewById(R.id.buttonManageAdd);

        buttonManageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ShelterManageActivity.this, ShelterEditActivity.class));
            }
        });
    }

    private List<Shelter> getShelters() {
        return FakeDatabase.getSheltersForStaff(sessionManager.getUserRole(), sessionManager.getUserEmail());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutManageList != null) {
            showShelters(getShelters());
            refreshHandler.postDelayed(refreshRunnable, REFRESH_MILLIS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void showShelters(List<Shelter> shelterList) {
        layoutManageList.removeAllViews();
        for (final Shelter shelter : shelterList) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_shelter_manage_row, layoutManageList, false);
            TextView textManageName = rowView.findViewById(R.id.textManageName);
            TextView textManageFloodTag = rowView.findViewById(R.id.textManageFloodTag);
            TextView textManagePlace = rowView.findViewById(R.id.textManagePlace);
            ProgressBar progressManage = rowView.findViewById(R.id.progressManage);
            TextView textManageCount = rowView.findViewById(R.id.textManageCount);
            TextView textManageUpdated = rowView.findViewById(R.id.textManageUpdated);

            textManageName.setText(shelter.getName());
            textManageFloodTag.setVisibility(shelter.isInFloodZone() ? View.VISIBLE : View.GONE);
            textManagePlace.setText(getString(R.string.shelter_manage_row, shelter.getVillage(), shelter.getType()));

            int colorId = getFullnessColorId(shelter.getPercentFull());
            int color = ContextCompat.getColor(this, colorId);
            progressManage.setProgress(Math.min(100, shelter.getPercentFull()));
            progressManage.setProgressTintList(ColorStateList.valueOf(color));
            textManageCount.setText(getString(R.string.shelter_manage_count, shelter.getPeopleInside(),
                    shelter.getCapacity(), shelter.getPercentFull()));
            textManageCount.setTextColor(color);
            textManageUpdated.setText(getString(R.string.shelter_manage_updated, shelter.getLastCountTime()));

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ShelterManageActivity.this, ShelterEditActivity.class);
                    intent.putExtra(ShelterEditActivity.EXTRA_SHELTER_ID, shelter.getId());
                    startActivity(intent);
                }
            });
            layoutManageList.addView(rowView);
        }
    }

    /** SH-02: green below 80%, orange from 80%, red at 100% or more. */
    public static int getFullnessColorId(int percentFull) {
        if (percentFull >= 100) return R.color.color_level_warning;
        if (percentFull >= 80) return R.color.color_level_watch;
        return R.color.color_level_all_clear;
    }
}
