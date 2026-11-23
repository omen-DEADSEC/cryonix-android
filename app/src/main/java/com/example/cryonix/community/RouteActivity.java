package com.example.cryonix.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.Shelter;

import java.util.List;

/**
 * A15 My route (CA-04): walking route from the approved evacuation plan, with time, distance
 * and steps. Opens for the shelter given in the intent, or the nearest shelter with room.
 */
public class RouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.SEE_ROUTE_AND_SHELTER)) {
            return;
        }
        setContentView(R.layout.activity_route);
        TopBarHelper.setup(this, R.string.route_title);

        VillagePack pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }

        Shelter shelter = pack.findShelter(getIntent().getStringExtra(ShelterDetailActivity.EXTRA_SHELTER_ID));
        if (shelter == null) {
            shelter = pack.getNearestShelterWithRoom();
        }
        if (shelter == null) {
            finish();
            return;
        }

        TextView textRouteTo = findViewById(R.id.textRouteTo);
        TextView textRouteSummary = findViewById(R.id.textRouteSummary);
        VillageMapView mapRoute = findViewById(R.id.mapRoute);
        LinearLayout layoutRouteSteps = findViewById(R.id.layoutRouteSteps);

        textRouteTo.setText(getString(R.string.route_to, shelter.getName()));
        textRouteSummary.setText(getString(R.string.route_summary,
                CommunityPackLoader.formatDistance(shelter.getDistanceMetres()), shelter.getWalkingMinutes()));

        mapRoute.setShelters(pack.shelters);
        mapRoute.setRouteShelter(shelter);

        List<String> steps = shelter.getRouteSteps();
        for (int i = 0; i < steps.size(); i++) {
            View stepView = LayoutInflater.from(this).inflate(R.layout.item_route_step, layoutRouteSteps, false);
            TextView textRouteStepNumber = stepView.findViewById(R.id.textRouteStepNumber);
            TextView textRouteStepText = stepView.findViewById(R.id.textRouteStepText);
            textRouteStepNumber.setText(String.valueOf(i + 1));
            textRouteStepText.setText(steps.get(i));
            layoutRouteSteps.addView(stepView);
        }
    }
}
