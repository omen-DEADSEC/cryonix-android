package com.example.cryonix.network;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.NetworkNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A29 Network health (LR-05). Roles with VIEW_NETWORK_HEALTH (admin, officer).
 * Every node with battery, signal and last report. Silent nodes are listed first, in red.
 */
public class NetworkHealthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_NETWORK_HEALTH)) {
            return;
        }
        setContentView(R.layout.activity_network_health);
        TopBarHelper.setup(this, R.string.network_title);

        TextView textNetworkSummary = findViewById(R.id.textNetworkSummary);
        TextView textNetworkRule = findViewById(R.id.textNetworkRule);
        LinearLayout layoutNetworkList = findViewById(R.id.layoutNetworkList);

        SessionManager sessionManager = new SessionManager(this);
        List<NetworkNode> nodeList = FakeDatabase.getNodesFor(sessionManager.getUserRole(), sessionManager.getUserEmail());

        // Silent nodes first, then low battery, then the rest
        List<NetworkNode> sortedList = new ArrayList<>();
        int onlineCount = 0;
        int silentCount = 0;
        int lowCount = 0;
        for (NetworkNode node : nodeList) {
            if (!node.isOnline()) {
                sortedList.add(0, node);
                silentCount++;
            } else {
                sortedList.add(node);
                onlineCount++;
            }
            if (node.isBatteryLow()) {
                lowCount++;
            }
        }

        textNetworkSummary.setText(getString(R.string.network_summary, onlineCount, silentCount, lowCount));
        textNetworkRule.setText(getString(R.string.network_rule, NetworkNode.REPORT_INTERVAL_MINUTES,
                NetworkNode.MISSED_REPORTS_FOR_SILENT));

        for (final NetworkNode node : sortedList) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_node_row, layoutNetworkList, false);
            ImageView imageNodeRowIcon = rowView.findViewById(R.id.imageNodeRowIcon);
            TextView textNodeRowName = rowView.findViewById(R.id.textNodeRowName);
            TextView textNodeRowTag = rowView.findViewById(R.id.textNodeRowTag);
            TextView textNodeRowDetails = rowView.findViewById(R.id.textNodeRowDetails);
            TextView textNodeRowLast = rowView.findViewById(R.id.textNodeRowLast);

            int iconColorId = node.isOnline() ? R.color.color_success : R.color.color_error;
            imageNodeRowIcon.setColorFilter(ContextCompat.getColor(this, iconColorId));
            textNodeRowName.setText(node.getId() + "  " + node.getVillage());
            if (!node.isOnline()) {
                textNodeRowTag.setText(R.string.network_silent_tag);
                textNodeRowTag.setVisibility(View.VISIBLE);
            } else if (node.isBatteryLow()) {
                textNodeRowTag.setText(R.string.network_low_tag);
                textNodeRowTag.setVisibility(View.VISIBLE);
            }
            textNodeRowDetails.setText(getString(R.string.network_row_details, node.getValley(),
                    node.getBatteryPercent(), node.getSignalWord(), node.getSignalDbm()));
            textNodeRowLast.setText(getString(R.string.network_row_last, node.getLastHeard()));

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openNode(NetworkHealthActivity.this, node.getId());
                }
            });
            layoutNetworkList.addView(rowView);
        }
    }

    public static void openNode(android.app.Activity activity, String nodeId) {
        Intent intent = new Intent(activity, NodeDetailActivity.class);
        intent.putExtra(NodeDetailActivity.EXTRA_NODE_ID, nodeId);
        activity.startActivity(intent);
    }
}
