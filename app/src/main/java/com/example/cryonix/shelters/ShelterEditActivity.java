package com.example.cryonix.shelters;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.example.cryonix.data.model.Shelter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Add or edit a shelter (SH-01): name, place, floor area, type, contact.
 * - Capacity is worked out from the floor area while typing.
 * - A shelter less than 20 m above the river gets a flood-zone warning.
 * For an existing shelter the live count is shown at the top (SH-02).
 */
public class ShelterEditActivity extends AppCompatActivity {

    public static final String EXTRA_SHELTER_ID = "shelter_id";

    private Shelter shelter;            // null while adding
    private List<String> villageNames;
    private String[] typeNames;

    private TextInputEditText editShelterName;
    private TextInputEditText editShelterArea;
    private TextInputEditText editShelterHeight;
    private TextInputEditText editShelterContact;
    private TextInputEditText editShelterPhone;
    private Spinner spinnerEditVillage;
    private Spinner spinnerEditType;
    private TextView textEditCapacity;
    private TextView textEditFloodWarning;
    private TextView textEditError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.MANAGE_SHELTERS)) {
            return;
        }
        setContentView(R.layout.activity_shelter_edit);

        SessionManager sessionManager = new SessionManager(this);
        villageNames = FakeDatabase.getVillageNamesForStaff(sessionManager.getUserRole(), sessionManager.getUserEmail());
        typeNames = getResources().getStringArray(R.array.shelter_types);

        String shelterId = getIntent().getStringExtra(EXTRA_SHELTER_ID);
        if (shelterId != null) {
            shelter = FakeDatabase.findShelter(shelterId);
        }
        TopBarHelper.setup(this, shelter == null ? R.string.shelter_edit_title_add : R.string.shelter_edit_title_edit);

        editShelterName = findViewById(R.id.editShelterName);
        editShelterArea = findViewById(R.id.editShelterArea);
        editShelterHeight = findViewById(R.id.editShelterHeight);
        editShelterContact = findViewById(R.id.editShelterContact);
        editShelterPhone = findViewById(R.id.editShelterPhone);
        spinnerEditVillage = findViewById(R.id.spinnerEditVillage);
        spinnerEditType = findViewById(R.id.spinnerEditType);
        textEditCapacity = findViewById(R.id.textEditCapacity);
        textEditFloodWarning = findViewById(R.id.textEditFloodWarning);
        textEditError = findViewById(R.id.textEditError);
        MaterialButton buttonEditSave = findViewById(R.id.buttonEditSave);

        ArrayAdapter<String> villageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, villageNames);
        villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditVillage.setAdapter(villageAdapter);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeNames);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditType.setAdapter(typeAdapter);

        if (shelter != null) {
            fillForm();
            showLiveCount();
        }

        TextWatcher checkWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCapacityAndWarning();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
        editShelterArea.addTextChangedListener(checkWatcher);
        editShelterHeight.addTextChangedListener(checkWatcher);
        updateCapacityAndWarning();

        buttonEditSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveShelter();
            }
        });
    }

    private void fillForm() {
        editShelterName.setText(shelter.getName());
        editShelterArea.setText(String.valueOf(shelter.getFloorAreaSquareMetres()));
        editShelterHeight.setText(String.valueOf(shelter.getHeightAboveRiverMetres()));
        editShelterContact.setText(shelter.getContactName());
        editShelterPhone.setText(shelter.getContactPhone());
        int villageIndex = villageNames.indexOf(shelter.getVillage());
        if (villageIndex >= 0) {
            spinnerEditVillage.setSelection(villageIndex);
        }
        for (int i = 0; i < typeNames.length; i++) {
            if (typeNames[i].equals(shelter.getType())) {
                spinnerEditType.setSelection(i);
            }
        }
    }

    /** SH-02: people in, people out, inside now and last update. */
    private void showLiveCount() {
        MaterialCardView cardEditLive = findViewById(R.id.cardEditLive);
        LinearLayout layoutEditLive = findViewById(R.id.layoutEditLive);
        cardEditLive.setVisibility(View.VISIBLE);
        layoutEditLive.removeAllViews();

        TextView textTitle = new TextView(this);
        textTitle.setText(R.string.shelter_edit_live_title);
        textTitle.setTextSize(16);
        textTitle.setTextColor(ContextCompat.getColor(this, R.color.color_text_primary));
        textTitle.setPadding(0, dp(8), 0, dp(4));
        layoutEditLive.addView(textTitle);

        addInfoRow(layoutEditLive, R.string.shelter_edit_in, String.valueOf(shelter.getPeopleCameIn()));
        addInfoRow(layoutEditLive, R.string.shelter_edit_out, String.valueOf(shelter.getPeopleWentOut()));
        addInfoRow(layoutEditLive, R.string.shelter_edit_present,
                getString(R.string.shelter_manage_count, shelter.getPeopleInside(), shelter.getCapacity(), shelter.getPercentFull()));
        addInfoRow(layoutEditLive, R.string.shelter_edit_last, shelter.getLastCountTime());
    }

    private int readNumber(TextInputEditText editText) {
        String text = editText.getText() == null ? "" : editText.getText().toString().trim();
        if (text.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateCapacityAndWarning() {
        int area = readNumber(editShelterArea);
        int height = readNumber(editShelterHeight);
        textEditCapacity.setText(getString(R.string.shelter_edit_capacity, area > 0 ? Shelter.capacityFromFloorArea(area) : 0));
        boolean isLow = height >= 0 && height < Shelter.SAFE_HEIGHT_METRES;
        textEditFloodWarning.setVisibility(isLow ? View.VISIBLE : View.GONE);
    }

    private void saveShelter() {
        String name = editShelterName.getText() == null ? "" : editShelterName.getText().toString().trim();
        int area = readNumber(editShelterArea);
        int height = readNumber(editShelterHeight);
        if (name.isEmpty() || area <= 0 || height < 0 || villageNames.isEmpty()) {
            textEditError.setVisibility(View.VISIBLE);
            return;
        }
        textEditError.setVisibility(View.GONE);

        boolean isNew = (shelter == null);
        if (isNew) {
            shelter = FakeDatabase.addShelter(new SessionManager(this).getUserName());
        }
        String contact = editShelterContact.getText() == null ? "" : editShelterContact.getText().toString().trim();
        String phone = editShelterPhone.getText() == null ? "" : editShelterPhone.getText().toString().trim();
        shelter.setDetails(name, villageNames.get(spinnerEditVillage.getSelectedItemPosition()),
                typeNames[spinnerEditType.getSelectedItemPosition()], area, height, contact, phone);

        String userName = new SessionManager(this).getUserName();
        String action = (isNew ? "Added shelter " : "Edited shelter ") + name + " (capacity " + shelter.getCapacity() + ")";
        if (shelter.isInFloodZone()) {
            action += " - in flood zone";
        }
        FakeDatabase.addAuditEntry(userName, action);
        Toast.makeText(this, R.string.shelter_edit_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void addInfoRow(LinearLayout parent, int labelId, String value) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_info_row, parent, false);
        TextView textInfoLabel = rowView.findViewById(R.id.textInfoLabel);
        TextView textInfoValue = rowView.findViewById(R.id.textInfoValue);
        textInfoLabel.setText(labelId);
        textInfoValue.setText(value);
        parent.addView(rowView);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
