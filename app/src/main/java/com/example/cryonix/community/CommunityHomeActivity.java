package com.example.cryonix.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityAlertHelper;
import com.example.cryonix.common.CommunityBottomBarHelper;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Shelter;
import com.example.cryonix.data.model.VillageAlert;
import com.example.cryonix.profile.ProfileSettingsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * A08 Community home.
 * CA-02: alert level of my village with colour, icon, label, last update and "what to do".
 * CA-07: when the newest alert is "All clear", it replaces the alert card.
 * CA-03: a new Watch or Warning alert opens the full-screen alert once.
 */
public class CommunityHomeActivity extends AppCompatActivity {

    private MaterialCardView cardCommunityHomeStatus;
    private TextView textCommunityHomeVillage;
    private TextView textCommunityHomeLevel;
    private TextView textCommunityHomeStatusTitle;
    private TextView textCommunityHomeWhatToDo;
    private TextView textCommunityHomeSafePoint;
    private TextView textCommunityHomeUpdated;
    private MaterialButton buttonCommunityHomeRoute;
    private MaterialButton buttonCommunityHomeOpenAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.RECEIVE_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_community_home);
        CommunityBottomBarHelper.setup(this, CommunityBottomBarHelper.TAB_HOME);

        cardCommunityHomeStatus = findViewById(R.id.cardCommunityHomeStatus);
        textCommunityHomeVillage = findViewById(R.id.textCommunityHomeVillage);
        textCommunityHomeLevel = findViewById(R.id.textCommunityHomeLevel);
        textCommunityHomeStatusTitle = findViewById(R.id.textCommunityHomeStatusTitle);
        textCommunityHomeWhatToDo = findViewById(R.id.textCommunityHomeWhatToDo);
        textCommunityHomeSafePoint = findViewById(R.id.textCommunityHomeSafePoint);
        textCommunityHomeUpdated = findViewById(R.id.textCommunityHomeUpdated);
        buttonCommunityHomeRoute = findViewById(R.id.buttonCommunityHomeRoute);
        buttonCommunityHomeOpenAlert = findViewById(R.id.buttonCommunityHomeOpenAlert);
        ImageButton buttonCommunityHomeSettings = findViewById(R.id.buttonCommunityHomeSettings);
        LinearLayout layoutCommunityHomeLinks = findViewById(R.id.layoutCommunityHomeLinks);

        buttonCommunityHomeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommunityHomeActivity.this, ProfileSettingsActivity.class));
            }
        });

        buttonCommunityHomeRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommunityHomeActivity.this, RouteActivity.class));
            }
        });

        addLinkRow(layoutCommunityHomeLinks, R.drawable.ic_lake, R.string.community_home_lake_link, LakeStatusActivity.class);
        addLinkRow(layoutCommunityHomeLinks, R.drawable.ic_book, R.string.community_home_guide_link, SafetyGuideActivity.class);
        addLinkRow(layoutCommunityHomeLinks, R.drawable.ic_node, R.string.node_message_link, NodeMessageActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cardCommunityHomeStatus == null) {
            return;
        }
        VillagePack pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }
        showStatus(pack);
    }

    private void showStatus(VillagePack pack) {
        textCommunityHomeVillage.setText(getString(R.string.community_home_village, pack.villageName));
        final VillageAlert activeAlert = pack.getActiveAlert();
        Shelter safePoint = pack.getNearestShelterWithRoom();

        if (safePoint != null) {
            textCommunityHomeSafePoint.setText(getString(R.string.community_home_safe_point,
                    safePoint.getName(), safePoint.getWalkingMinutes()));
            textCommunityHomeSafePoint.setVisibility(View.VISIBLE);
        } else {
            textCommunityHomeSafePoint.setVisibility(View.GONE);
        }

        if (activeAlert != null) {
            // ----- Alert for my village -----
            AlertLevel level = activeAlert.getLevel();
            LevelBadgeHelper.show(textCommunityHomeLevel, level);
            textCommunityHomeStatusTitle.setText(level.getLabel() + ": " + activeAlert.getLakeName());
            textCommunityHomeWhatToDo.setText(getWhatToDo(level));
            textCommunityHomeUpdated.setText(getString(R.string.community_home_updated, activeAlert.getTime()));
            setCardBorder(level.getColorId(), 3);

            buttonCommunityHomeOpenAlert.setVisibility(View.VISIBLE);
            buttonCommunityHomeOpenAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(CommunityAlertHelper.makeFullScreenIntent(CommunityHomeActivity.this, activeAlert.getId(), false));
                }
            });

            // A new Watch or Warning opens the full-screen alert once
            if (level.isSerious()) {
                CommunityAlertHelper.showIfNew(this, activeAlert, safePoint == null ? null : safePoint.getName());
            }
        } else if (pack.isAllClear()) {
            // ----- All clear (CA-07) -----
            LevelBadgeHelper.show(textCommunityHomeLevel, AlertLevel.ALL_CLEAR);
            textCommunityHomeStatusTitle.setText(R.string.community_home_all_clear_title);
            textCommunityHomeWhatToDo.setText(R.string.community_home_all_clear_text);
            textCommunityHomeUpdated.setText(getString(R.string.community_home_updated, pack.getLatestAlert().getTime()));
            setCardBorder(AlertLevel.ALL_CLEAR.getColorId(), 3);
            buttonCommunityHomeOpenAlert.setVisibility(View.GONE);
        } else {
            // ----- Normal times -----
            LevelBadgeHelper.show(textCommunityHomeLevel, pack.lakeLevel);
            textCommunityHomeStatusTitle.setText(R.string.community_home_no_alert_title);
            textCommunityHomeWhatToDo.setText(R.string.community_home_no_alert_text);
            textCommunityHomeUpdated.setText(getString(R.string.community_home_updated, pack.syncedAt));
            setCardBorder(R.color.color_border, 1);
            buttonCommunityHomeOpenAlert.setVisibility(View.GONE);
        }
    }

    private String getWhatToDo(AlertLevel level) {
        if (level == AlertLevel.WARNING) {
            return getString(R.string.what_to_do_warning);
        } else if (level == AlertLevel.WATCH) {
            return getString(R.string.what_to_do_watch);
        }
        return getString(R.string.what_to_do_advisory);
    }

    private void setCardBorder(int colorId, int widthDp) {
        cardCommunityHomeStatus.setStrokeColor(ContextCompat.getColor(this, colorId));
        cardCommunityHomeStatus.setStrokeWidth(Math.round(widthDp * getResources().getDisplayMetrics().density));
    }

    private void addLinkRow(LinearLayout parent, int iconId, int titleId, final Class<?> screenClass) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_menu_row, parent, false);
        ImageView imageMenuRowIcon = rowView.findViewById(R.id.imageMenuRowIcon);
        TextView textMenuRowTitle = rowView.findViewById(R.id.textMenuRowTitle);
        TextView textMenuRowText = rowView.findViewById(R.id.textMenuRowText);
        imageMenuRowIcon.setImageResource(iconId);
        textMenuRowTitle.setText(titleId);
        textMenuRowText.setVisibility(View.GONE);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommunityHomeActivity.this, screenClass));
            }
        });
        parent.addView(rowView);
    }
}
