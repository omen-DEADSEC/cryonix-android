package com.example.cryonix.community;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityBottomBarHelper;
import com.example.cryonix.common.CommunityPackLoader;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.VillagePack;
import com.example.cryonix.data.model.Shelter;

/**
 * A10 Shelters near me (CA-06): distance, walking time, capacity and people inside.
 * DEMO: people inside will come from the shelter camera counter (Shelter Management epic).
 */
public class SheltersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.SEE_ROUTE_AND_SHELTER)) {
            return;
        }
        setContentView(R.layout.activity_shelters);
        TopBarHelper.setupWithoutBack(this, R.string.shelters_title);
        CommunityBottomBarHelper.setup(this, CommunityBottomBarHelper.TAB_SHELTERS);

        VillagePack pack = CommunityPackLoader.load(this);
        if (pack == null) {
            return;
        }

        LinearLayout layoutSheltersList = findViewById(R.id.layoutSheltersList);
        for (final Shelter shelter : pack.shelters) {
            View rowView = makeShelterRow(this, layoutSheltersList, shelter);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SheltersActivity.this, ShelterDetailActivity.class);
                    intent.putExtra(ShelterDetailActivity.EXTRA_SHELTER_ID, shelter.getId());
                    startActivity(intent);
                }
            });
            layoutSheltersList.addView(rowView);
        }
    }

    public static View makeShelterRow(Context context, ViewGroup parent, Shelter shelter) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_shelter_row, parent, false);
        TextView textShelterRowName = rowView.findViewById(R.id.textShelterRowName);
        TextView textShelterRowDetails = rowView.findViewById(R.id.textShelterRowDetails);
        ProgressBar progressShelterRow = rowView.findViewById(R.id.progressShelterRow);
        TextView textShelterRowFree = rowView.findViewById(R.id.textShelterRowFree);

        textShelterRowName.setText(shelter.getName());
        textShelterRowDetails.setText(context.getString(R.string.shelters_row_details,
                CommunityPackLoader.formatDistance(shelter.getDistanceMetres()), shelter.getWalkingMinutes()));

        // Bar shows how full the shelter is
        progressShelterRow.setProgress(shelter.getPeopleInside() * 100 / Math.max(1, shelter.getCapacity()));
        int colorId = shelter.isFull() ? R.color.color_error : R.color.color_success;
        progressShelterRow.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorId)));

        if (shelter.isFull()) {
            textShelterRowFree.setText(R.string.shelters_full);
        } else {
            textShelterRowFree.setText(context.getString(R.string.shelters_free, shelter.getFreePlaces(), shelter.getCapacity()));
        }
        textShelterRowFree.setTextColor(ContextCompat.getColor(context, colorId));
        return rowView;
    }
}
