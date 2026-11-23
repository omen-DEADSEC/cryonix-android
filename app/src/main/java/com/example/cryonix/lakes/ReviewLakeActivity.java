package com.example.cryonix.lakes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.alerts.CreateAlertActivity;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.LevelBadgeHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.SessionManager;
import com.example.cryonix.common.TopBarHelper;
import com.example.cryonix.common.UserRole;
import com.example.cryonix.data.FakeDatabase;
import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Lake;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * A23 Review flagged lake (DR-02). Only roles with REVIEW_LAKES (admin, officer).
 * The officer confirms (and changes the level), keeps the level, or dismisses the flag.
 * A note is required and the decision goes to the audit log.
 */
public class ReviewLakeActivity extends AppCompatActivity {

    private static final int MIN_NOTE_LENGTH = 10;

    private Lake lake;
    private RadioGroup radioGroupReviewDecision;
    private LinearLayout layoutReviewNewLevel;
    private Spinner spinnerReviewNewLevel;
    private TextInputEditText editReviewNote;
    private TextView textReviewError;

    private final AlertLevel[] levelChoices = AlertLevel.lakeLevels();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.REVIEW_LAKES)) {
            return;
        }

        lake = FakeDatabase.findLake(getIntent().getStringExtra(LakeDetailActivity.EXTRA_LAKE_ID));
        if (lake == null || !lake.isFlagged()) {
            Toast.makeText(this, R.string.review_not_flagged, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_review_lake);
        TopBarHelper.setup(this, R.string.review_title);

        TextView textReviewLevel = findViewById(R.id.textReviewLevel);
        TextView textReviewLakeName = findViewById(R.id.textReviewLakeName);
        TextView textReviewFlagReason = findViewById(R.id.textReviewFlagReason);
        radioGroupReviewDecision = findViewById(R.id.radioGroupReviewDecision);
        layoutReviewNewLevel = findViewById(R.id.layoutReviewNewLevel);
        spinnerReviewNewLevel = findViewById(R.id.spinnerReviewNewLevel);
        editReviewNote = findViewById(R.id.editReviewNote);
        textReviewError = findViewById(R.id.textReviewError);
        MaterialButton buttonReviewSave = findViewById(R.id.buttonReviewSave);

        LakeOutlineView outlineReview = findViewById(R.id.outlineReview);
        outlineReview.setLake(lake.getAreaHectares(), lake.getPreviousArea(), lake.getPastMaxArea(),
                lake.getId().hashCode() & 0xFF);

        LevelBadgeHelper.show(textReviewLevel, lake.getLevel());
        textReviewLakeName.setText(lake.getName());
        textReviewFlagReason.setText(lake.getFlagReason() + "\n"
                + getString(R.string.lake_detail_confidence, lake.getModelConfidencePercent()));

        // Level spinner: Warning, Watch, Advisory, Baseline. Start on one level higher than now.
        String[] levelNames = new String[levelChoices.length];
        int startIndex = 0;
        for (int i = 0; i < levelChoices.length; i++) {
            levelNames[i] = levelChoices[i].getLabel();
            if (levelChoices[i] == lake.getLevel()) {
                startIndex = Math.max(0, i - 1);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levelNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReviewNewLevel.setAdapter(adapter);
        spinnerReviewNewLevel.setSelection(startIndex);

        radioGroupReviewDecision.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                layoutReviewNewLevel.setVisibility(checkedId == R.id.radioReviewConfirm ? View.VISIBLE : View.GONE);
            }
        });

        buttonReviewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReview();
            }
        });
    }

    private void saveReview() {
        int checkedId = radioGroupReviewDecision.getCheckedRadioButtonId();
        String note = editReviewNote.getText() == null ? "" : editReviewNote.getText().toString().trim();

        if (checkedId == -1) {
            showError(R.string.review_error_choice);
            return;
        }
        if (note.length() < MIN_NOTE_LENGTH) {
            showError(R.string.review_error_note);
            return;
        }
        textReviewError.setVisibility(View.GONE);

        SessionManager sessionManager = new SessionManager(this);
        String reviewerName = sessionManager.getUserName();
        AlertLevel newLevel = null;
        String decision;

        if (checkedId == R.id.radioReviewConfirm) {
            newLevel = levelChoices[spinnerReviewNewLevel.getSelectedItemPosition()];
            decision = "flag confirmed";
        } else if (checkedId == R.id.radioReviewKeep) {
            decision = "level kept";
        } else {
            decision = "flag rejected";
        }

        FakeDatabase.reviewLake(lake, decision, newLevel, note, reviewerName);
        Toast.makeText(this, getString(R.string.review_saved_by, reviewerName, lake.getLastReviewTime()), Toast.LENGTH_LONG).show();

        // If the lake is now Watch or Warning, offer to send an alert straight away
        UserRole role = sessionManager.getUserRole();
        if (newLevel != null && newLevel.isSerious() && role != null && role.can(Permission.CREATE_ALERTS)) {
            askToCreateAlert(newLevel);
        } else {
            finish();
        }
    }

    private void askToCreateAlert(AlertLevel newLevel) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.review_send_alert_question, lake.getName(), newLevel.getLabel()))
                .setPositiveButton(R.string.review_send_alert_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ReviewLakeActivity.this, CreateAlertActivity.class);
                        intent.putExtra(LakeDetailActivity.EXTRA_LAKE_ID, lake.getId());
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.review_send_alert_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showError(int messageId) {
        textReviewError.setText(messageId);
        textReviewError.setVisibility(View.VISIBLE);
    }
}
