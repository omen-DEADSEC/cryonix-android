package com.example.cryonix.signup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.Village;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * A06 Choose village (community sign-up step 2 of 3).
 * Also opened from Settings to change the village (EXTRA_FROM_SETTINGS = true).
 */
public class ChooseVillageActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_SETTINGS = "from_settings";
    public static final String EXTRA_VILLAGE_NAME = "village_name";

    private LinearLayout layoutChooseVillageList;
    private TextView textChooseVillageEmpty;
    private MaterialButton buttonChooseVillageContinue;

    private String selectedVillageName = null;
    private boolean isFromSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_village);
        TopBarHelper.setup(this, R.string.choose_village_title);

        isFromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);
        if (isFromSettings) {
            selectedVillageName = new SessionManager(this).getUserVillage();
        }

        layoutChooseVillageList = findViewById(R.id.layoutChooseVillageList);
        textChooseVillageEmpty = findViewById(R.id.textChooseVillageEmpty);
        buttonChooseVillageContinue = findViewById(R.id.buttonChooseVillageContinue);
        TextInputEditText editChooseVillageSearch = findViewById(R.id.editChooseVillageSearch);

        if (isFromSettings) {
            buttonChooseVillageContinue.setText(R.string.button_save);
        }

        editChooseVillageSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showVillages(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        MaterialButton buttonChooseVillageLocation = findViewById(R.id.buttonChooseVillageLocation);
        buttonChooseVillageLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useMyLocation();
            }
        });

        buttonChooseVillageContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueWithVillage();
            }
        });

        showVillages("");
    }

    /** Builds the list again, keeping only villages whose name or valley matches the search text. */
    private void showVillages(String searchText) {
        layoutChooseVillageList.removeAllViews();
        String search = searchText.trim().toLowerCase();
        int shownCount = 0;

        List<Village> villageList = FakeDatabase.getVillages();
        for (final Village village : villageList) {
            boolean matches = village.getName().toLowerCase().contains(search)
                    || village.getValley().toLowerCase().contains(search);
            if (!matches) {
                continue;
            }

            View rowView = LayoutInflater.from(this).inflate(R.layout.item_village, layoutChooseVillageList, false);
            TextView textVillageName = rowView.findViewById(R.id.textVillageName);
            TextView textVillageArea = rowView.findViewById(R.id.textVillageArea);
            ImageView imageVillageSelected = rowView.findViewById(R.id.imageVillageSelected);

            textVillageName.setText(village.getName());
            textVillageArea.setText(village.getAreaText());

            boolean isSelected = village.getName().equals(selectedVillageName);
            imageVillageSelected.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            rowView.setSelected(isSelected);

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedVillageName = village.getName();
                    buttonChooseVillageContinue.setEnabled(true);
                    TextInputEditText editSearch = findViewById(R.id.editChooseVillageSearch);
                    showVillages(editSearch.getText() == null ? "" : editSearch.getText().toString());
                }
            });

            layoutChooseVillageList.addView(rowView);
            shownCount++;
        }

        textChooseVillageEmpty.setVisibility(shownCount == 0 ? View.VISIBLE : View.GONE);
        buttonChooseVillageContinue.setEnabled(selectedVillageName != null);
    }

    private void continueWithVillage() {
        if (selectedVillageName == null) {
            return;
        }

        if (isFromSettings) {
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.setUserVillage(selectedVillageName);
            // MN-01: a number is linked to one village, so move the own number to the new village
            String myPhone = sessionManager.getUserPhone();
            if (FakeDatabase.isPhoneRegistered(myPhone)) {
                FakeDatabase.registerPhone(myPhone, selectedVillageName, getString(R.string.calls_owner_me), myPhone);
            }
            FakeDatabase.addAuditEntry(sessionManager.getUserName(), "Changed village to " + selectedVillageName);
            Toast.makeText(this, R.string.message_saved, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent intent = new Intent(this, RegisterPhoneActivity.class);
        intent.putExtra(EXTRA_VILLAGE_NAME, selectedVillageName);
        startActivity(intent);
    }

    /**
     * CA-01: choose the village by location.
     * Uses the last position the phone already knows (fast, works offline with GPS).
     */
    private void useMyLocation() {
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            Toast.makeText(this, R.string.choose_village_location_permission, Toast.LENGTH_LONG).show();
            return;
        }

        Location location = null;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        } catch (SecurityException e) {
            location = null;
        }

        String nearestVillage = null;
        if (location != null) {
            nearestVillage = FakeDatabase.findNearestVillage(location.getLatitude(), location.getLongitude(), 30);
        }

        if (nearestVillage == null) {
            Toast.makeText(this, R.string.choose_village_location_none, Toast.LENGTH_LONG).show();
            return;
        }
        selectedVillageName = nearestVillage;
        Toast.makeText(this, getString(R.string.choose_village_location_found, nearestVillage), Toast.LENGTH_SHORT).show();
        TextInputEditText editSearch = findViewById(R.id.editChooseVillageSearch);
        editSearch.setText("");
        showVillages("");
    }
}
