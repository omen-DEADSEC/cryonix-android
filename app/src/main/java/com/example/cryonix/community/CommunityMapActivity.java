package com.example.cryonix.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityBottomBarHelper;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.Shelter;
import com.google.android.material.button.MaterialButton;

/** A09 Map of my village: flood zone, shelters and my route, saved on the phone (CA-05, CA-06). */
public class CommunityMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.SEE_ROUTE_AND_SHELTER)) {
            return;
        }
        setContentView(R.layout.activity_community_map);
        TopBarHelper.setupWithoutBack(this, R.string.map_title);
        CommunityBottomBarHelper.setup(this, CommunityBottomBarHelper.TAB_MAP);

        VillagePack pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }

        VillageMapView mapCommunity = findViewById(R.id.mapCommunity);
        MaterialButton buttonCommunityMapRoute = findViewById(R.id.buttonCommunityMapRoute);

        mapCommunity.setShelters(pack.shelters);
        mapCommunity.setRouteShelter(pack.getNearestShelterWithRoom());
        mapCommunity.setOnShelterTapListener(new VillageMapView.OnShelterTapListener() {
            @Override
            public void onShelterTapped(Shelter shelter) {
                Intent intent = new Intent(CommunityMapActivity.this, ShelterDetailActivity.class);
                intent.putExtra(ShelterDetailActivity.EXTRA_SHELTER_ID, shelter.getId());
                startActivity(intent);
            }
        });

        buttonCommunityMapRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommunityMapActivity.this, RouteActivity.class));
            }
        });
    }
}
