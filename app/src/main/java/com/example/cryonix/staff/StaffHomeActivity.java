package com.example.cryonix.staff;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.cryonix.R;
import com.example.cryonix.alerts.AlertsListActivity;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.NotificationHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.StaffBottomBarHelper;
import com.example.cryonix.common.ThemeManager;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.Alert;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Lake;
import com.example.cryonix.data.model.NetworkNode;
import com.example.cryonix.data.model.StaffNotice;
import com.example.cryonix.lakes.LakeDetailActivity;
import com.example.cryonix.lakes.LakesListActivity;
import com.example.cryonix.network.NetworkHealthActivity;
import com.example.cryonix.notifications.NotificationsActivity;

import java.util.List;
import java.util.Locale;

/**
 * A20 Staff home (DR-01 key numbers and DR-02 staff functions in the app).
 * Used by System admin, Authority officer and Organisation staff.
 * - Organisation staff see only their organisation's valleys.
 * - "Lakes to review" shows only for roles allowed to review lakes.
 */
public class StaffHomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_NOTIFICATIONS = 201;

    // Show the phone notification only once each time the app is opened
    private static boolean summaryShownThisRun = false;

    private SessionManager sessionManager;
    private UserRole currentRole;
    private String userEmail;

    private TextView textStaffHomeBellCount;
    private LinearLayout layoutStaffHomeLevels;
    private LinearLayout layoutStaffHomeReview;
    private LinearLayout layoutStaffHomeReviewList;
    private TextView textStaffHomeNoReview;
    private View tileStaffHomeLakes;
    private View tileStaffHomeAlerts;
    private View tileStaffHomeNodes;
    private View tileStaffHomePeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        currentRole = sessionManager.getUserRole();
        if (!sessionManager.isLoggedIn() || currentRole == null || !currentRole.isStaff()) {
            // Community members or signed-out users must not see this screen
            ScreenRouter.openHomeForRole(this, currentRole);
            return;
        }
        userEmail = sessionManager.getUserEmail();

        setContentView(R.layout.activity_staff_home);
        StaffBottomBarHelper.setup(this, StaffBottomBarHelper.TAB_HOME);

        TextView textStaffHomeGreeting = findViewById(R.id.textStaffHomeGreeting);
        TextView textStaffHomeRole = findViewById(R.id.textStaffHomeRole);
        TextView textStaffHomeValleys = findViewById(R.id.textStaffHomeValleys);
        ImageButton buttonStaffHomeTheme = findViewById(R.id.buttonStaffHomeTheme);
        ImageButton buttonStaffHomeBell = findViewById(R.id.buttonStaffHomeBell);
        textStaffHomeBellCount = findViewById(R.id.textStaffHomeBellCount);
        layoutStaffHomeLevels = findViewById(R.id.layoutStaffHomeLevels);
        layoutStaffHomeReview = findViewById(R.id.layoutStaffHomeReview);
        layoutStaffHomeReviewList = findViewById(R.id.layoutStaffHomeReviewList);
        textStaffHomeNoReview = findViewById(R.id.textStaffHomeNoReview);
        tileStaffHomeLakes = findViewById(R.id.tileStaffHomeLakes);
        tileStaffHomeAlerts = findViewById(R.id.tileStaffHomeAlerts);
        tileStaffHomeNodes = findViewById(R.id.tileStaffHomeNodes);
        tileStaffHomePeople = findViewById(R.id.tileStaffHomePeople);

        textStaffHomeGreeting.setText(getString(R.string.staff_home_greeting, sessionManager.getUserName()));
        textStaffHomeRole.setText(currentRole.getDisplayName());

        // Organisation staff: say which valleys they can see
        List<String> visibleValleys = FakeDatabase.getVisibleValleys(currentRole, userEmail);
        if (visibleValleys != null) {
            textStaffHomeValleys.setText(getString(R.string.staff_home_valleys_note, TextUtils.join(", ", visibleValleys)));
            textStaffHomeValleys.setVisibility(View.VISIBLE);
        }

        buttonStaffHomeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThemeManager.toggleLightDark(StaffHomeActivity.this);
            }
        });

        buttonStaffHomeBell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StaffHomeActivity.this, NotificationsActivity.class));
            }
        });

        askNotificationPermissionIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager == null || layoutStaffHomeLevels == null) {
            return;
        }
        // Numbers can change on other screens (review, new alert), so refresh each time
        showKeyNumbers();
        showLevelCounts();
        showLakesToReview();
        showBellCount();
    }

    // ---------------- Key numbers ----------------

    private void showKeyNumbers() {
        List<Lake> lakeList = FakeDatabase.getLakesFor(currentRole, userEmail);
        List<Alert> alertList = FakeDatabase.getAlertsFor(currentRole, userEmail);
        final List<NetworkNode> silentNodes = FakeDatabase.getSilentNodesFor(currentRole, userEmail);

        int seriousCount = 0;
        for (Lake lake : lakeList) {
            if (lake.getLevel().isSerious()) seriousCount++;
        }

        setTile(tileStaffHomeLakes, R.drawable.ic_lake, String.valueOf(seriousCount),
                getString(R.string.staff_home_tile_lakes), getString(R.string.staff_home_tile_lakes_sub, lakeList.size()));
        setTile(tileStaffHomeAlerts, R.drawable.ic_alert_bell, String.valueOf(FakeDatabase.countActiveAlerts(alertList)),
                getString(R.string.staff_home_tile_alerts), getString(R.string.staff_home_tile_alerts_sub));
        setTile(tileStaffHomeNodes, R.drawable.ic_node, String.valueOf(silentNodes.size()),
                getString(R.string.staff_home_tile_nodes),
                getString(R.string.staff_home_tile_nodes_sub, FakeDatabase.countNodesFor(currentRole, userEmail)));
        setTile(tileStaffHomePeople, R.drawable.ic_people,
                String.format(Locale.ENGLISH, "%,d", FakeDatabase.getPeopleAtRisk(lakeList)),
                getString(R.string.staff_home_tile_people), getString(R.string.staff_home_tile_people_sub));

        // Each number opens its detail
        tileStaffHomeLakes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StaffHomeActivity.this, LakesListActivity.class));
                overridePendingTransition(0, 0);
            }
        });
        tileStaffHomeAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StaffHomeActivity.this, AlertsListActivity.class));
                overridePendingTransition(0, 0);
            }
        });
        tileStaffHomeNodes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // LR-05: roles that can see network health open the full screen
                if (currentRole.can(Permission.VIEW_NETWORK_HEALTH)) {
                    startActivity(new Intent(StaffHomeActivity.this, NetworkHealthActivity.class));
                } else {
                    showSilentNodes(silentNodes);
                }
            }
        });
        tileStaffHomePeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StaffHomeActivity.this, R.string.staff_home_people_coming, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setTile(View tileView, int iconId, String value, String label, String subText) {
        ImageView imageKeyNumberIcon = tileView.findViewById(R.id.imageKeyNumberIcon);
        TextView textKeyNumberValue = tileView.findViewById(R.id.textKeyNumberValue);
        TextView textKeyNumberLabel = tileView.findViewById(R.id.textKeyNumberLabel);
        TextView textKeyNumberSub = tileView.findViewById(R.id.textKeyNumberSub);

        imageKeyNumberIcon.setImageResource(iconId);
        textKeyNumberValue.setText(value);
        textKeyNumberLabel.setText(label);
        textKeyNumberSub.setText(subText);
    }

    private void showSilentNodes(List<NetworkNode> silentNodes) {
        if (silentNodes.isEmpty()) {
            Toast.makeText(this, R.string.staff_home_nodes_all_ok, Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder message = new StringBuilder();
        for (NetworkNode node : silentNodes) {
            message.append(getString(R.string.node_details, node.getVillage(), node.getValley(),
                    node.getLastHeard(), node.getBatteryPercent(), getString(R.string.node_silent)));
            message.append("\n\n");
        }
        message.append(getString(R.string.node_more_coming));

        new AlertDialog.Builder(this)
                .setTitle(R.string.staff_home_tile_nodes)
                .setMessage(message.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    // ---------------- Lakes by level ----------------

    private void showLevelCounts() {
        layoutStaffHomeLevels.removeAllViews();
        List<Lake> lakeList = FakeDatabase.getLakesFor(currentRole, userEmail);

        for (final AlertLevel level : AlertLevel.lakeLevels()) {
            int count = 0;
            for (Lake lake : lakeList) {
                if (lake.getLevel() == level) count++;
            }

            TextView textLevel = new TextView(this);
            LevelBadgeHelper.show(textLevel, level);
            textLevel.setText(level.getLabel() + "  " + count);
            textLevel.setTextSize(14);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(Math.round(8 * getResources().getDisplayMetrics().density));
            textLevel.setLayoutParams(params);

            // Tap a level to open the lakes list already filtered
            textLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(StaffHomeActivity.this, LakesListActivity.class);
                    intent.putExtra(LakesListActivity.EXTRA_LEVEL, level.name());
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
            layoutStaffHomeLevels.addView(textLevel);
        }
    }

    // ---------------- Lakes to review ----------------

    private void showLakesToReview() {
        if (!currentRole.can(Permission.REVIEW_LAKES)) {
            layoutStaffHomeReview.setVisibility(View.GONE);
            return;
        }
        layoutStaffHomeReview.setVisibility(View.VISIBLE);
        layoutStaffHomeReviewList.removeAllViews();

        int flaggedCount = 0;
        for (final Lake lake : FakeDatabase.getLakesFor(currentRole, userEmail)) {
            if (!lake.isFlagged()) {
                continue;
            }
            flaggedCount++;
            View rowView = LakesListActivity.makeLakeRow(this, layoutStaffHomeReviewList, lake);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(StaffHomeActivity.this, LakeDetailActivity.class);
                    intent.putExtra(LakeDetailActivity.EXTRA_LAKE_ID, lake.getId());
                    startActivity(intent);
                }
            });
            layoutStaffHomeReviewList.addView(rowView);
        }
        textStaffHomeNoReview.setVisibility(flaggedCount == 0 ? View.VISIBLE : View.GONE);
    }

    // ---------------- Notifications ----------------

    private void showBellCount() {
        List<StaffNotice> noticeList = FakeDatabase.getNoticesFor(currentRole, userEmail);
        int unreadCount = FakeDatabase.countUnread(noticeList);

        if (unreadCount > 0) {
            textStaffHomeBellCount.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
            textStaffHomeBellCount.setVisibility(View.VISIBLE);
        } else {
            textStaffHomeBellCount.setVisibility(View.GONE);
        }

        // Phone notification once per app run, if the user wants lake and node notices
        boolean wantsNotices = sessionManager.getNotificationSetting(SessionManager.KEY_NOTIFY_NODE_HEALTH);
        if (!summaryShownThisRun && wantsNotices && unreadCount > 0) {
            String firstTitle = "";
            for (StaffNotice notice : noticeList) {
                if (!notice.isRead()) {
                    firstTitle = notice.getTitle();
                    break;
                }
            }
            NotificationHelper.showUnreadSummary(this, unreadCount, firstTitle);
            summaryShownThisRun = true;
        }
    }

    private void askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && !NotificationHelper.canPostNotifications(this)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            summaryShownThisRun = false;   // try again now that we may have permission
            showBellCount();
        }
    }
}
