package com.example.cryonix.alerts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.example.cryonix.data.model.Alert;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Lake;
import com.example.cryonix.lakes.LakeDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A27 Create alert (DR-02). Only roles with CREATE_ALERTS (admin, officer).
 * Steps: lake -> level -> villages -> channels -> message -> confirm -> delivery screen.
 * DEMO: the alert engine is not ready, so FakeDatabase "sends" it.
 */
public class CreateAlertActivity extends AppCompatActivity {

    private final AlertLevel[] levelChoices = AlertLevel.alertLevels();
    private final String[] channelChoices = {Alert.CHANNEL_LORA, Alert.CHANNEL_APP, Alert.CHANNEL_CELL, Alert.CHANNEL_VOICE};

    private List<Lake> lakeList;
    private Spinner spinnerCreateAlertLake;
    private RadioGroup radioGroupCreateAlertLevel;
    private LinearLayout layoutCreateAlertVillages;
    private LinearLayout layoutCreateAlertChannels;
    private TextInputEditText editCreateAlertMessage;
    private TextView textCreateAlertError;

    private final List<CheckBox> villageCheckBoxes = new ArrayList<>();
    private final List<CheckBox> channelCheckBoxes = new ArrayList<>();
    private final int[] radioIds = {R.id.radioCreateAlertLevel1, R.id.radioCreateAlertLevel2,
            R.id.radioCreateAlertLevel3, R.id.radioCreateAlertLevel4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.CREATE_ALERTS)) {
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        lakeList = FakeDatabase.getLakesFor(sessionManager.getUserRole(), sessionManager.getUserEmail());
        if (lakeList.isEmpty()) {
            Toast.makeText(this, R.string.create_alert_no_lakes, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_create_alert);
        TopBarHelper.setup(this, R.string.create_alert_title);

        spinnerCreateAlertLake = findViewById(R.id.spinnerCreateAlertLake);
        radioGroupCreateAlertLevel = findViewById(R.id.radioGroupCreateAlertLevel);
        layoutCreateAlertVillages = findViewById(R.id.layoutCreateAlertVillages);
        layoutCreateAlertChannels = findViewById(R.id.layoutCreateAlertChannels);
        editCreateAlertMessage = findViewById(R.id.editCreateAlertMessage);
        textCreateAlertError = findViewById(R.id.textCreateAlertError);
        MaterialButton buttonCreateAlertSend = findViewById(R.id.buttonCreateAlertSend);

        // Lake spinner
        String[] lakeNames = new String[lakeList.size()];
        int startLakeIndex = 0;
        String wantedLakeId = getIntent().getStringExtra(LakeDetailActivity.EXTRA_LAKE_ID);
        for (int i = 0; i < lakeList.size(); i++) {
            lakeNames[i] = lakeList.get(i).getName() + " (" + lakeList.get(i).getLevel().getLabel() + ")";
            if (lakeList.get(i).getId().equals(wantedLakeId)) {
                startLakeIndex = i;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lakeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCreateAlertLake.setAdapter(adapter);
        spinnerCreateAlertLake.setSelection(startLakeIndex);

        // Level radios
        for (int i = 0; i < levelChoices.length; i++) {
            RadioButton radioButton = findViewById(radioIds[i]);
            radioButton.setText(levelChoices[i].getLabel());
        }

        // Channel check boxes (all ticked to start)
        for (String channel : channelChoices) {
            CheckBox checkBoxChannel = new CheckBox(this);
            checkBoxChannel.setText(channel);
            checkBoxChannel.setChecked(true);
            checkBoxChannel.setMinHeight(Math.round(48 * getResources().getDisplayMetrics().density));
            layoutCreateAlertChannels.addView(checkBoxChannel);
            channelCheckBoxes.add(checkBoxChannel);
        }

        spinnerCreateAlertLake.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onLakeChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        radioGroupCreateAlertLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                fillMessageTemplate();
            }
        });

        // Start on the lake's own level when it is one of the alert levels
        selectLevel(getSelectedLake().getLevel());
        onLakeChanged();

        buttonCreateAlertSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndConfirm();
            }
        });
    }

    private Lake getSelectedLake() {
        return lakeList.get(spinnerCreateAlertLake.getSelectedItemPosition());
    }

    private AlertLevel getSelectedLevel() {
        int checkedId = radioGroupCreateAlertLevel.getCheckedRadioButtonId();
        for (int i = 0; i < radioIds.length; i++) {
            if (radioIds[i] == checkedId) {
                return levelChoices[i];
            }
        }
        return null;
    }

    private void selectLevel(AlertLevel level) {
        for (int i = 0; i < levelChoices.length; i++) {
            if (levelChoices[i] == level) {
                radioGroupCreateAlertLevel.check(radioIds[i]);
            }
        }
    }

    /** New lake: show its downstream villages (all ticked) and refresh the message. */
    private void onLakeChanged() {
        layoutCreateAlertVillages.removeAllViews();
        villageCheckBoxes.clear();
        for (String villageName : getSelectedLake().getDownstreamVillages()) {
            CheckBox checkBoxVillage = new CheckBox(this);
            checkBoxVillage.setText(villageName);
            checkBoxVillage.setChecked(true);
            checkBoxVillage.setMinHeight(Math.round(48 * getResources().getDisplayMetrics().density));
            layoutCreateAlertVillages.addView(checkBoxVillage);
            villageCheckBoxes.add(checkBoxVillage);
        }
        fillMessageTemplate();
    }

    /** Ready-made message for the chosen level. The officer can still edit it. */
    private void fillMessageTemplate() {
        AlertLevel level = getSelectedLevel();
        if (level == null) {
            return;
        }
        String lakeName = getSelectedLake().getName();
        int templateId;
        if (level == AlertLevel.ADVISORY) {
            templateId = R.string.alert_template_advisory;
        } else if (level == AlertLevel.WATCH) {
            templateId = R.string.alert_template_watch;
        } else if (level == AlertLevel.WARNING) {
            templateId = R.string.alert_template_warning;
        } else {
            templateId = R.string.alert_template_all_clear;
        }
        editCreateAlertMessage.setText(getString(templateId, lakeName));
    }

    private List<String> getCheckedTexts(List<CheckBox> checkBoxes) {
        List<String> checkedTexts = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                checkedTexts.add(checkBox.getText().toString());
            }
        }
        return checkedTexts;
    }

    private void checkAndConfirm() {
        final AlertLevel level = getSelectedLevel();
        final List<String> villageNames = getCheckedTexts(villageCheckBoxes);
        final List<String> channels = getCheckedTexts(channelCheckBoxes);
        final String message = editCreateAlertMessage.getText() == null ? "" : editCreateAlertMessage.getText().toString().trim();

        if (level == null) {
            showError(R.string.create_alert_error_level);
            return;
        }
        if (villageNames.isEmpty()) {
            showError(R.string.create_alert_error_villages);
            return;
        }
        if (channels.isEmpty()) {
            showError(R.string.create_alert_error_channels);
            return;
        }
        if (message.isEmpty()) {
            showError(R.string.create_alert_error_message);
            return;
        }
        textCreateAlertError.setVisibility(View.GONE);

        final Lake lake = getSelectedLake();
        new AlertDialog.Builder(this)
                .setTitle(R.string.create_alert_preview_title)
                .setMessage(getString(R.string.create_alert_preview, level.getLabel(), lake.getName(),
                        TextUtils.join(", ", villageNames),
                        String.format(Locale.ENGLISH, "%,d", FakeDatabase.countPeople(villageNames)),
                        TextUtils.join(", ", channels), message))
                .setPositiveButton(R.string.create_alert_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendAlert(lake, level, villageNames, channels, message);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void sendAlert(Lake lake, AlertLevel level, List<String> villageNames, List<String> channels, String message) {
        String senderName = new SessionManager(this).getUserName();
        Alert alert = FakeDatabase.sendAlert(lake, level, villageNames, channels, message, senderName);
        Toast.makeText(this, R.string.create_alert_sent, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, AlertDeliveryActivity.class);
        intent.putExtra(AlertDeliveryActivity.EXTRA_ALERT_ID, alert.getId());
        startActivity(intent);
        finish();
    }

    private void showError(int messageId) {
        textCreateAlertError.setText(messageId);
        textCreateAlertError.setVisibility(View.VISIBLE);
    }
}
