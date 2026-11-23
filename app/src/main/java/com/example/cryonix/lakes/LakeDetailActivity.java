package com.example.cryonix.lakes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.cryonix.data.model.Lake;
import com.example.cryonix.flood.FloodScenarioActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * A22 Lake detail.
 * "Review flag" shows only for roles with REVIEW_LAKES, "Create alert" only for CREATE_ALERTS.
 */
public class LakeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LAKE_ID = "lake_id";

    private String lakeId;
    private UserRole currentRole;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.VIEW_LAKES)) {
            return;
        }
        setContentView(R.layout.activity_lake_detail);
        TopBarHelper.setup(this, R.string.lake_detail_title);

        SessionManager sessionManager = new SessionManager(this);
        currentRole = sessionManager.getUserRole();
        userEmail = sessionManager.getUserEmail();
        lakeId = getIntent().getStringExtra(EXTRA_LAKE_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentRole != null) {
            showLake();   // refresh after a review
        }
    }

    /** Organisation staff may only open lakes in their own valleys. */
    private Lake findVisibleLake() {
        for (Lake lake : FakeDatabase.getLakesFor(currentRole, userEmail)) {
            if (lake.getId().equals(lakeId)) {
                return lake;
            }
        }
        return null;
    }

    private void showLake() {
        final Lake lake = findVisibleLake();
        if (lake == null) {
            Toast.makeText(this, R.string.lake_detail_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView textLakeDetailLevel = findViewById(R.id.textLakeDetailLevel);
        TextView textLakeDetailName = findViewById(R.id.textLakeDetailName);
        TextView textLakeDetailId = findViewById(R.id.textLakeDetailId);
        LinearLayout layoutLakeDetailFlag = findViewById(R.id.layoutLakeDetailFlag);
        TextView textLakeDetailFlagReason = findViewById(R.id.textLakeDetailFlagReason);
        TextView textLakeDetailConfidence = findViewById(R.id.textLakeDetailConfidence);
        LinearLayout layoutLakeDetailInfo = findViewById(R.id.layoutLakeDetailInfo);
        TextView textLakeDetailReviewNote = findViewById(R.id.textLakeDetailReviewNote);
        MaterialButton buttonLakeDetailReview = findViewById(R.id.buttonLakeDetailReview);
        MaterialButton buttonLakeDetailCreateAlert = findViewById(R.id.buttonLakeDetailCreateAlert);

        LevelBadgeHelper.show(textLakeDetailLevel, lake.getLevel());
        textLakeDetailName.setText(lake.getName());
        textLakeDetailId.setText(lake.getId());

        if (lake.isFlagged()) {
            layoutLakeDetailFlag.setVisibility(View.VISIBLE);
            textLakeDetailFlagReason.setText(lake.getFlagReason());
            textLakeDetailConfidence.setText(getString(R.string.lake_detail_confidence, lake.getModelConfidencePercent()));
        } else {
            layoutLakeDetailFlag.setVisibility(View.GONE);
        }

        layoutLakeDetailInfo.removeAllViews();
        addInfoRow(layoutLakeDetailInfo, R.string.lake_detail_area, LakesListActivity.formatNumber(lake.getAreaHectares()) + " ha");
        addInfoRow(layoutLakeDetailInfo, R.string.lake_detail_change, LakesListActivity.formatChange(lake.getAreaChangePercent()));
        addInfoRow(layoutLakeDetailInfo, R.string.lake_detail_elevation, lake.getElevationMetres() + " m");
        addInfoRow(layoutLakeDetailInfo, R.string.lake_detail_place, lake.getValley() + ", " + lake.getDistrict());
        addInfoRow(layoutLakeDetailInfo, R.string.lake_detail_image, lake.getLastImageDate());
        addInfoRow(layoutLakeDetailInfo, R.string.lake_detail_villages, TextUtils.join(", ", lake.getDownstreamVillages()));

        // LM-05: area history chart with the past maximum
        AreaChartView chartLakeDetailArea = findViewById(R.id.chartLakeDetailArea);
        chartLakeDetailArea.setData(lake.getHistoryMonths(), lake.getAreaHistory(), lake.getPastMaxArea());

        // LM-05: previous and latest image side by side
        LakeOutlineView outlineLakeDetailPrevious = findViewById(R.id.outlineLakeDetailPrevious);
        LakeOutlineView outlineLakeDetailLatest = findViewById(R.id.outlineLakeDetailLatest);
        TextView textLakeDetailImagePrevious = findViewById(R.id.textLakeDetailImagePrevious);
        TextView textLakeDetailImageLatest = findViewById(R.id.textLakeDetailImageLatest);
        int shapeSeed = lake.getId().hashCode() & 0xFF;
        double biggest = lake.getPastMaxArea();
        outlineLakeDetailPrevious.setLake(lake.getPreviousArea(), 0, biggest, shapeSeed);
        outlineLakeDetailLatest.setLake(lake.getAreaHectares(), lake.getPreviousArea(), biggest, shapeSeed);
        textLakeDetailImagePrevious.setText(getString(R.string.lake_detail_image_previous,
                lake.getPreviousImageDate(), LakesListActivity.formatNumber(lake.getPreviousArea())));
        textLakeDetailImageLatest.setText(getString(R.string.lake_detail_image_latest,
                lake.getLastImageDate(), LakesListActivity.formatNumber(lake.getAreaHectares())));

        // LM-05: notes
        LinearLayout layoutLakeDetailNotes = findViewById(R.id.layoutLakeDetailNotes);
        layoutLakeDetailNotes.removeAllViews();
        addInfoRow(layoutLakeDetailNotes, R.string.lake_detail_glacier, lake.getGlacierNote());
        addInfoRow(layoutLakeDetailNotes, R.string.lake_detail_slope, lake.getSlopeNote());
        addInfoRow(layoutLakeDetailNotes, R.string.lake_detail_dam, lake.getDamNote());

        // LM-07: last review with decision, user and time
        if (!lake.getLastReviewBy().isEmpty()) {
            String record = getString(R.string.lake_detail_review_record, lake.getLastReviewDecision(),
                    lake.getLastReviewBy(), lake.getLastReviewTime())
                    + "\n" + getString(R.string.lake_detail_review_note, lake.getLastReviewNote());
            textLakeDetailReviewNote.setText(record);
            textLakeDetailReviewNote.setVisibility(View.VISIBLE);
        } else {
            textLakeDetailReviewNote.setVisibility(View.GONE);
        }

        // Buttons follow the role
        boolean canReview = currentRole.can(Permission.REVIEW_LAKES) && lake.isFlagged();
        buttonLakeDetailReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
        buttonLakeDetailCreateAlert.setVisibility(currentRole.can(Permission.CREATE_ALERTS) ? View.VISIBLE : View.GONE);

        // FS-01: flood scenarios button for roles that may see them
        MaterialButton buttonLakeDetailFlood = findViewById(R.id.buttonLakeDetailFlood);
        buttonLakeDetailFlood.setVisibility(currentRole.can(Permission.VIEW_FLOOD_SCENARIOS) ? View.VISIBLE : View.GONE);
        buttonLakeDetailFlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LakeDetailActivity.this, FloodScenarioActivity.class);
                intent.putExtra(EXTRA_LAKE_ID, lake.getId());
                startActivity(intent);
            }
        });

        buttonLakeDetailReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LakeDetailActivity.this, ReviewLakeActivity.class);
                intent.putExtra(EXTRA_LAKE_ID, lake.getId());
                startActivity(intent);
            }
        });

        buttonLakeDetailCreateAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LakeDetailActivity.this, CreateAlertActivity.class);
                intent.putExtra(EXTRA_LAKE_ID, lake.getId());
                startActivity(intent);
            }
        });
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
