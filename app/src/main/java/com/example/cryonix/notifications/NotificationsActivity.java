package com.example.cryonix.notifications;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.alerts.AlertDeliveryActivity;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.NetworkNode;
import com.example.cryonix.data.model.StaffNotice;
import com.example.cryonix.lakes.LakeDetailActivity;
import com.example.cryonix.network.NetworkHealthActivity;

import java.util.List;

/**
 * A33 Notifications (DR-04): flagged lakes, silent nodes and sent alerts.
 * Tapping a notice marks it read and opens the item it is about.
 * Staff only; organisation staff see notices of their own valleys only.
 */
public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout layoutNotificationsList;
    private TextView textNotificationsEmpty;
    private UserRole currentRole;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        currentRole = sessionManager.getUserRole();
        if (!sessionManager.isLoggedIn() || currentRole == null || !currentRole.isStaff()) {
            // Opened from the phone notification after sign out, or by a community member
            ScreenRouter.openHomeForRole(this, currentRole);
            return;
        }
        userEmail = sessionManager.getUserEmail();

        setContentView(R.layout.activity_notifications);
        TopBarHelper.setup(this, R.string.notifications_title);

        layoutNotificationsList = findViewById(R.id.layoutNotificationsList);
        textNotificationsEmpty = findViewById(R.id.textNotificationsEmpty);
        TextView textNotificationsMarkAll = findViewById(R.id.textNotificationsMarkAll);

        textNotificationsMarkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (StaffNotice notice : FakeDatabase.getNoticesFor(currentRole, userEmail)) {
                    notice.setRead(true);
                }
                showNotices();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutNotificationsList != null) {
            showNotices();
        }
    }

    private void showNotices() {
        layoutNotificationsList.removeAllViews();
        List<StaffNotice> noticeList = FakeDatabase.getNoticesFor(currentRole, userEmail);
        textNotificationsEmpty.setVisibility(noticeList.isEmpty() ? View.VISIBLE : View.GONE);

        for (final StaffNotice notice : noticeList) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_notice_row, layoutNotificationsList, false);
            ImageView imageNoticeIcon = rowView.findViewById(R.id.imageNoticeIcon);
            TextView textNoticeTitle = rowView.findViewById(R.id.textNoticeTitle);
            TextView textNoticeText = rowView.findViewById(R.id.textNoticeText);
            TextView textNoticeTime = rowView.findViewById(R.id.textNoticeTime);
            View viewNoticeUnreadDot = rowView.findViewById(R.id.viewNoticeUnreadDot);

            imageNoticeIcon.setImageResource(getIconFor(notice.getType()));
            textNoticeTitle.setText(notice.getTitle());
            textNoticeTitle.setTypeface(null, notice.isRead() ? Typeface.NORMAL : Typeface.BOLD);
            textNoticeText.setText(notice.getText());
            textNoticeTime.setText(notice.getTime());
            viewNoticeUnreadDot.setVisibility(notice.isRead() ? View.INVISIBLE : View.VISIBLE);

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notice.setRead(true);
                    openItem(notice);
                }
            });
            layoutNotificationsList.addView(rowView);
        }
    }

    private int getIconFor(String type) {
        if (StaffNotice.TYPE_LAKE_FLAGGED.equals(type)) {
            return R.drawable.ic_lake;
        } else if (StaffNotice.TYPE_NODE_SILENT.equals(type)) {
            return R.drawable.ic_node;
        }
        return R.drawable.ic_alert_bell;
    }

    /** Opens the lake, the alert, or the node details. */
    private void openItem(StaffNotice notice) {
        if (StaffNotice.TYPE_LAKE_FLAGGED.equals(notice.getType())) {
            Intent intent = new Intent(this, LakeDetailActivity.class);
            intent.putExtra(LakeDetailActivity.EXTRA_LAKE_ID, notice.getItemId());
            startActivity(intent);
        } else if (StaffNotice.TYPE_ALERT_SENT.equals(notice.getType())) {
            Intent intent = new Intent(this, AlertDeliveryActivity.class);
            intent.putExtra(AlertDeliveryActivity.EXTRA_ALERT_ID, Integer.parseInt(notice.getItemId()));
            startActivity(intent);
        } else if (currentRole.can(Permission.VIEW_NETWORK_HEALTH)) {
            NetworkHealthActivity.openNode(this, notice.getItemId());   // LR-08 node page
        } else {
            showNodeDialog(notice.getItemId());
        }
    }

    private void showNodeDialog(String nodeId) {
        NetworkNode node = FakeDatabase.findNode(nodeId);
        if (node == null) {
            showNotices();
            return;
        }
        String status = getString(node.isOnline() ? R.string.node_online : R.string.node_silent);
        String message = getString(R.string.node_details, node.getVillage(), node.getValley(),
                node.getLastHeard(), node.getBatteryPercent(), status)
                + "\n\n" + getString(R.string.node_more_coming);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.node_title, node.getId()))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        showNotices();   // the dot disappears now that it is read
    }
}
