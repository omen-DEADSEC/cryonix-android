package com.example.cryonix.network;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.NetworkNode;
import com.example.cryonix.lakes.AreaChartView;

import java.util.Locale;

/**
 * A30 Node detail (LR-08): position, battery and signal over 24 hours, and the last messages,
 * so that repairs can be planned.
 */
public class NodeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NODE_ID = "node_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_NETWORK_HEALTH)) {
            return;
        }
        NetworkNode node = FakeDatabase.findNode(getIntent().getStringExtra(EXTRA_NODE_ID));
        if (node == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_node_detail);
        TopBarHelper.setup(this, R.string.node_detail_title);

        TextView textNodeDetailName = findViewById(R.id.textNodeDetailName);
        TextView textNodeDetailRepair = findViewById(R.id.textNodeDetailRepair);
        LinearLayout layoutNodeDetailInfo = findViewById(R.id.layoutNodeDetailInfo);
        AreaChartView chartNodeBattery = findViewById(R.id.chartNodeBattery);
        AreaChartView chartNodeSignal = findViewById(R.id.chartNodeSignal);
        LinearLayout layoutNodeDetailMessages = findViewById(R.id.layoutNodeDetailMessages);

        textNodeDetailName.setText(node.getId() + "  " + node.getVillage());
        boolean needsVisit = !node.isOnline() || node.isBatteryLow();
        textNodeDetailRepair.setVisibility(needsVisit ? View.VISIBLE : View.GONE);

        addInfoRow(layoutNodeDetailInfo, R.string.node_detail_village, node.getVillage() + ", " + node.getValley());
        addInfoRow(layoutNodeDetailInfo, R.string.node_detail_position,
                String.format(Locale.ENGLISH, "%.4f N, %.4f E", node.getLatitude(), node.getLongitude()));
        addInfoRow(layoutNodeDetailInfo, R.string.node_detail_status,
                getString(node.isOnline() ? R.string.node_online : R.string.node_silent));
        addInfoRow(layoutNodeDetailInfo, R.string.node_detail_missed, String.valueOf(node.getMissedReports()));

        // 0 = no "past maximum" line on these charts
        chartNodeBattery.setData(node.getHourLabels(), node.getBatteryHistory(), 0);
        chartNodeSignal.setData(node.getHourLabels(), node.getSignalHistory(), 0);

        for (String message : node.getLastMessages()) {
            TextView textMessage = new TextView(this);
            textMessage.setText(message);
            textMessage.setTextSize(14);
            textMessage.setTextColor(ContextCompat.getColor(this, R.color.color_text_primary));
            textMessage.setBackgroundResource(R.drawable.bg_list_row);
            int pad = Math.round(12 * getResources().getDisplayMetrics().density);
            textMessage.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = Math.round(8 * getResources().getDisplayMetrics().density);
            textMessage.setLayoutParams(params);
            layoutNodeDetailMessages.addView(textMessage);
        }
    }

    private void addInfoRow(LinearLayout parent, int labelId, String value) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_info_row, parent, false);
        TextView textInfoLabel = rowView.findViewById(R.id.textInfoLabel);
        TextView textInfoValue = rowView.findViewById(R.id.textInfoValue);
        textInfoLabel.setText(labelId);
        textInfoValue.setText(value);
        parent.addView(rowView);
    }
}
