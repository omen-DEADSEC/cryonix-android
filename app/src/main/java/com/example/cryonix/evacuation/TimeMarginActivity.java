package com.example.cryonix.evacuation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.Lake;
import com.example.cryonix.data.model.VillageTimeMargin;
import com.example.cryonix.lakes.LakeDetailActivity;

import java.util.List;

/**
 * A25 Evacuation time margin (EV-02). Only roles with PLAN_EVACUATION (admin, officer).
 * Shows, for the worst-case flood of a lake, how many houses in each village have
 * enough, tight or no time to reach safety, so officers know who needs help first.
 */
public class TimeMarginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.PLAN_EVACUATION)) {
            return;
        }
        Lake lake = FakeDatabase.findLake(getIntent().getStringExtra(LakeDetailActivity.EXTRA_LAKE_ID));
        if (lake == null) {
            Toast.makeText(this, R.string.lake_detail_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_time_margin);
        TopBarHelper.setup(this, R.string.margin_title);

        TextView textMarginLake = findViewById(R.id.textMarginLake);
        LinearLayout layoutMarginFormula = findViewById(R.id.layoutMarginFormula);
        TextView textMarginTotals = findViewById(R.id.textMarginTotals);
        LinearLayout layoutMarginVillages = findViewById(R.id.layoutMarginVillages);

        textMarginLake.setText(lake.getName());

        // The fixed parts of the time budget
        addFormulaLine(layoutMarginFormula, getString(R.string.margin_part_detection, FakeDatabase.DETECTION_MINUTES));
        addFormulaLine(layoutMarginFormula, getString(R.string.margin_part_alert, FakeDatabase.ALERT_MINUTES));
        addFormulaLine(layoutMarginFormula, getString(R.string.margin_part_ready, FakeDatabase.GET_READY_MINUTES));
        addFormulaLine(layoutMarginFormula, getString(R.string.margin_part_walk));

        List<VillageTimeMargin> villageList = FakeDatabase.getTimeMargins(lake);
        int totalEnough = 0;
        int totalTight = 0;
        int totalNone = 0;

        for (VillageTimeMargin villageMargin : villageList) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_village_margin, layoutMarginVillages, false);
            TextView textMarginVillage = cardView.findViewById(R.id.textMarginVillage);
            TextView textMarginArrival = cardView.findViewById(R.id.textMarginArrival);
            TextView textMarginEnough = cardView.findViewById(R.id.textMarginEnough);
            TextView textMarginTight = cardView.findViewById(R.id.textMarginTight);
            TextView textMarginNone = cardView.findViewById(R.id.textMarginNone);
            HouseMarginView viewMarginHouses = cardView.findViewById(R.id.viewMarginHouses);

            int enough = villageMargin.countHouses(VillageTimeMargin.CLASS_ENOUGH);
            int tight = villageMargin.countHouses(VillageTimeMargin.CLASS_TIGHT);
            int none = villageMargin.countHouses(VillageTimeMargin.CLASS_NONE);
            totalEnough += enough;
            totalTight += tight;
            totalNone += none;

            textMarginVillage.setText(villageMargin.getVillage());
            textMarginArrival.setText(getString(R.string.margin_arrival, villageMargin.getArrivalMinutes(),
                    villageMargin.getHouseMargins().size()));
            showCount(textMarginEnough, R.string.margin_enough, enough, VillageTimeMargin.CLASS_ENOUGH);
            showCount(textMarginTight, R.string.margin_tight, tight, VillageTimeMargin.CLASS_TIGHT);
            showCount(textMarginNone, R.string.margin_none, none, VillageTimeMargin.CLASS_NONE);
            viewMarginHouses.setMargins(villageMargin.getHouseMargins());

            layoutMarginVillages.addView(cardView);
        }

        textMarginTotals.setText(getString(R.string.margin_totals, totalEnough, totalTight, totalNone));
    }

    /** "Enough 52" in green, "Tight 9" in yellow, "No time 3" in red. Colour AND label. */
    private void showCount(TextView textView, int labelId, int count, int houseClass) {
        textView.setText(getString(labelId, count));
        textView.setTextColor(ContextCompat.getColor(this, VillageTimeMargin.getColorIdOf(houseClass)));
    }

    private void addFormulaLine(LinearLayout parent, String text) {
        TextView textLine = new TextView(this);
        textLine.setText(text);
        textLine.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));
        textLine.setTextSize(14);
        textLine.setPadding(0, Math.round(4 * getResources().getDisplayMetrics().density), 0, 0);
        parent.addView(textLine);
    }
}
