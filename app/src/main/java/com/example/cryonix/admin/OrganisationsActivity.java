package com.example.cryonix.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.Organisation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Organisations (AC-08). Admin adds organisations and links them to valleys.
 */
public class OrganisationsActivity extends AppCompatActivity {

    private LinearLayout layoutOrganisationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.MANAGE_ORGANISATIONS)) {
            return;
        }
        setContentView(R.layout.activity_organisations);
        TopBarHelper.setup(this, R.string.organisations_title);

        layoutOrganisationsList = findViewById(R.id.layoutOrganisationsList);
        MaterialButton buttonOrganisationsAdd = findViewById(R.id.buttonOrganisationsAdd);

        buttonOrganisationsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });

        showOrganisations();
    }

    private void showOrganisations() {
        layoutOrganisationsList.removeAllViews();
        for (Organisation organisation : FakeDatabase.getOrganisations()) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_organisation, layoutOrganisationsList, false);
            TextView textOrganisationName = rowView.findViewById(R.id.textOrganisationName);
            TextView textOrganisationType = rowView.findViewById(R.id.textOrganisationType);
            TextView textOrganisationValleys = rowView.findViewById(R.id.textOrganisationValleys);

            textOrganisationName.setText(organisation.getName());
            textOrganisationType.setText(organisation.getType());
            textOrganisationValleys.setText(getString(R.string.organisations_valleys,
                    TextUtils.join(", ", organisation.getValleys())));

            layoutOrganisationsList.addView(rowView);
        }
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_organisation, null);
        final TextInputEditText editAddOrganisationName = dialogView.findViewById(R.id.editAddOrganisationName);
        final Spinner spinnerAddOrganisationType = dialogView.findViewById(R.id.spinnerAddOrganisationType);
        final LinearLayout layoutAddOrganisationValleys = dialogView.findViewById(R.id.layoutAddOrganisationValleys);
        final TextView textAddOrganisationError = dialogView.findViewById(R.id.textAddOrganisationError);

        // One check box for each valley
        final List<CheckBox> valleyCheckBoxes = new ArrayList<>();
        for (String valleyName : FakeDatabase.getValleyNames()) {
            CheckBox checkBoxValley = new CheckBox(this);
            checkBoxValley.setText(valleyName);
            layoutAddOrganisationValleys.addView(checkBoxValley);
            valleyCheckBoxes.add(checkBoxValley);
        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.organisations_add)
                .setView(dialogView)
                .setPositiveButton(R.string.button_save, null)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
        dialog.show();

        // Set the click here so the dialog stays open when there is an error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editAddOrganisationName.getText() == null ? "" : editAddOrganisationName.getText().toString().trim();
                List<String> chosenValleys = new ArrayList<>();
                for (CheckBox checkBox : valleyCheckBoxes) {
                    if (checkBox.isChecked()) {
                        chosenValleys.add(checkBox.getText().toString());
                    }
                }

                if (name.isEmpty() || chosenValleys.isEmpty()) {
                    textAddOrganisationError.setVisibility(View.VISIBLE);
                    return;
                }

                String type = spinnerAddOrganisationType.getSelectedItem().toString();
                String adminName = new SessionManager(OrganisationsActivity.this).getUserName();
                FakeDatabase.addOrganisation(new Organisation(name, type, chosenValleys), adminName);
                Toast.makeText(OrganisationsActivity.this, R.string.organisations_added, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showOrganisations();
            }
        });
    }
}
