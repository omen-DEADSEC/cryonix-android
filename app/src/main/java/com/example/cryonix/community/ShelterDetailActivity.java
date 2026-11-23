package com.example.cryonix.community;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.Shelter;
import com.google.android.material.button.MaterialButton;

/**
 * A11 Shelter detail (CA-06): places, people inside and facilities.
 * A16: if the shelter is full, "Show route" offers the nearest shelter that has room.
 */
public class ShelterDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SHELTER_ID = "shelter_id";

    private VillagePack pack;
    private Shelter shelter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.SEE_ROUTE_AND_SHELTER)) {
            return;
        }
        setContentView(R.layout.activity_shelter_detail);
        TopBarHelper.setup(this, R.string.shelter_detail_title);

        pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }
        shelter = pack.findShelter(getIntent().getStringExtra(EXTRA_SHELTER_ID));
        if (shelter == null) {
            finish();
            return;
        }

        TextView textShelterDetailName = findViewById(R.id.textShelterDetailName);
        TextView textShelterDetailFullTag = findViewById(R.id.textShelterDetailFullTag);
        LinearLayout layoutShelterDetailInfo = findViewById(R.id.layoutShelterDetailInfo);
        LinearLayout layoutShelterDetailFacilities = findViewById(R.id.layoutShelterDetailFacilities);
        MaterialButton buttonShelterDetailRoute = findViewById(R.id.buttonShelterDetailRoute);

        textShelterDetailName.setText(shelter.getName());
        textShelterDetailFullTag.setVisibility(shelter.isFull() ? View.VISIBLE : View.GONE);

        addInfoRow(layoutShelterDetailInfo, R.string.shelter_detail_distance, CommunityPackLoader.formatDistance(shelter.getDistanceMetres()));
        addInfoRow(layoutShelterDetailInfo, R.string.shelter_detail_walk, getString(R.string.minutes_value, shelter.getWalkingMinutes()));
        addInfoRow(layoutShelterDetailInfo, R.string.shelter_detail_capacity, String.valueOf(shelter.getCapacity()));
        addInfoRow(layoutShelterDetailInfo, R.string.shelter_detail_inside, String.valueOf(shelter.getPeopleInside()));
        addInfoRow(layoutShelterDetailInfo, R.string.shelter_detail_free, String.valueOf(shelter.getFreePlaces()));

        // Facilities with a tick in front
        for (String facility : shelter.getFacilities()) {
            TextView textFacility = new TextView(this);
            textFacility.setText(facility);
            textFacility.setTextSize(16);
            textFacility.setTextColor(ContextCompat.getColor(this, R.color.color_text_primary));
            textFacility.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0);
            textFacility.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_success)));
            textFacility.setCompoundDrawablePadding(Math.round(8 * getResources().getDisplayMetrics().density));
            int pad = Math.round(8 * getResources().getDisplayMetrics().density);
            textFacility.setPadding(0, pad, 0, pad);
            layoutShelterDetailFacilities.addView(textFacility);
        }

        buttonShelterDetailRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shelter.isFull()) {
                    showFullRedirect();
                } else {
                    openRoute(shelter);
                }
            }
        });
    }

    /** A16 Shelter full: send the person to the nearest shelter with room. */
    private void showFullRedirect() {
        final Shelter other = pack.getNearestShelterWithRoom();
        if (other == null || other.isFull()) {
            Toast.makeText(this, R.string.shelter_full_none, Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.shelter_full_title)
                .setMessage(getString(R.string.shelter_full_text, other.getName(), other.getFreePlaces(),
                        CommunityPackLoader.formatDistance(other.getDistanceMetres())))
                .setPositiveButton(R.string.shelter_full_go, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openRoute(other);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void openRoute(Shelter target) {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra(EXTRA_SHELTER_ID, target.getId());
        startActivity(intent);
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
