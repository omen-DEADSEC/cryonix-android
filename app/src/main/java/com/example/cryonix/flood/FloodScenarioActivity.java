package com.example.cryonix.flood;

import android.content.Intent;
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
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.FloodScenario;
import com.example.cryonix.data.model.Lake;
import com.example.cryonix.data.model.SettlementArrival;
import com.example.cryonix.evacuation.TimeMarginActivity;
import com.google.android.material.button.MaterialButton;
import com.example.cryonix.lakes.LakeDetailActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;
import java.util.Locale;

/**
 * A24 Flood scenarios (FS-01, FS-03). Roles with VIEW_FLOOD_SCENARIOS (admin, officer, staff).
 * - Small, medium and worst case for the lake, each with release volume, model and run date.
 * - Arrival time at each village as a range, a coloured valley drawing and a list.
 */
public class FloodScenarioActivity extends AppCompatActivity {

    private Lake lake;
    private List<FloodScenario> scenarioList;

    private LinearLayout layoutFloodInfo;
    private LinearLayout layoutFloodArrivals;
    private FloodArrivalMapView mapFloodArrival;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_FLOOD_SCENARIOS)) {
            return;
        }

        // Organisation staff may only open lakes in their own valleys
        SessionManager sessionManager = new SessionManager(this);
        String lakeId = getIntent().getStringExtra(LakeDetailActivity.EXTRA_LAKE_ID);
        for (Lake oneLake : FakeDatabase.getLakesFor(sessionManager.getUserRole(), sessionManager.getUserEmail())) {
            if (oneLake.getId().equals(lakeId)) {
                lake = oneLake;
            }
        }
        if (lake == null) {
            Toast.makeText(this, R.string.lake_detail_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_flood_scenario);
        TopBarHelper.setup(this, R.string.flood_title);

        TextView textFloodLakeName = findViewById(R.id.textFloodLakeName);
        layoutFloodInfo = findViewById(R.id.layoutFloodInfo);
        layoutFloodArrivals = findViewById(R.id.layoutFloodArrivals);
        mapFloodArrival = findViewById(R.id.mapFloodArrival);
        MaterialButtonToggleGroup toggleFloodScenario = findViewById(R.id.toggleFloodScenario);

        textFloodLakeName.setText(lake.getName());
        scenarioList = FakeDatabase.getFloodScenarios(lake);

        toggleFloodScenario.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (!isChecked) {
                    return;
                }
                if (checkedId == R.id.buttonFloodSmall) {
                    showScenario(scenarioList.get(0));
                } else if (checkedId == R.id.buttonFloodMedium) {
                    showScenario(scenarioList.get(1));
                } else {
                    showScenario(scenarioList.get(2));
                }
            }
        });

        // EV-02 button only for roles that plan evacuation
        MaterialButton buttonFloodTimeMargin = findViewById(R.id.buttonFloodTimeMargin);
        if (sessionManager.getUserRole().can(Permission.PLAN_EVACUATION)) {
            buttonFloodTimeMargin.setVisibility(View.VISIBLE);
        }
        buttonFloodTimeMargin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FloodScenarioActivity.this, TimeMarginActivity.class);
                intent.putExtra(LakeDetailActivity.EXTRA_LAKE_ID, lake.getId());
                startActivity(intent);
            }
        });

        // Worst case first: that is the one used for planning
        showScenario(scenarioList.get(2));
    }

    private void showScenario(FloodScenario scenario) {
        layoutFloodInfo.removeAllViews();
        addInfoRow(R.string.flood_release, String.format(Locale.ENGLISH, "%.2f million m³", scenario.getReleaseMillionCubicMetres()));
        addInfoRow(R.string.flood_peak, String.format(Locale.ENGLISH, "%,d m³/s", scenario.getPeakFlowCubicMetresPerSecond()));
        addInfoRow(R.string.flood_model, scenario.getModelName());
        addInfoRow(R.string.flood_run_date, scenario.getRunDate());

        mapFloodArrival.setArrivals(lake.getId(), scenario.getArrivals());

        layoutFloodArrivals.removeAllViews();
        for (SettlementArrival arrival : scenario.getArrivals()) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_arrival_row, layoutFloodArrivals, false);
            View viewArrivalBand = rowView.findViewById(R.id.viewArrivalBand);
            TextView textArrivalVillage = rowView.findViewById(R.id.textArrivalVillage);
            TextView textArrivalDetails = rowView.findViewById(R.id.textArrivalDetails);
            TextView textArrivalTime = rowView.findViewById(R.id.textArrivalTime);

            viewArrivalBand.setBackgroundColor(ContextCompat.getColor(this, arrival.getBandColorId()));
            textArrivalVillage.setText(arrival.getVillage());
            textArrivalDetails.setText(getString(R.string.flood_arrival_details,
                    String.format(Locale.ENGLISH, "%.0f", arrival.getDistanceKm()),
                    String.format(Locale.ENGLISH, "%,d", arrival.getPopulation())));
            textArrivalTime.setText(getString(R.string.flood_arrival_range, arrival.getEarliestMinutes(), arrival.getLatestMinutes()));
            layoutFloodArrivals.addView(rowView);
        }
    }

    private void addInfoRow(int labelId, String value) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_info_row, layoutFloodInfo, false);
        TextView textInfoLabel = rowView.findViewById(R.id.textInfoLabel);
        TextView textInfoValue = rowView.findViewById(R.id.textInfoValue);
        textInfoLabel.setText(labelId);
        textInfoValue.setText(value);
        layoutFloodInfo.addView(rowView);
    }
}
