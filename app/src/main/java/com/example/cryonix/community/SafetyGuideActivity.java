package com.example.cryonix.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.AccessGuard;
import com.example.cryonix.common.CommunityAlertHelper;
import com.example.cryonix.common.Permission;
import com.example.cryonix.common.TopBarHelper;
import com.google.android.material.button.MaterialButton;

/**
 * A19 Safety guide (CA-09). All text and pictures are inside the app, so it opens without internet.
 * "Practise the alert screen" opens the full-screen alert marked PRACTICE ONLY.
 */
public class SafetyGuideActivity extends AppCompatActivity {

    // Picture, title and text for each step (same order)
    private final int[] stepIcons = {R.drawable.ic_bag, R.drawable.ic_route, R.drawable.ic_speaker,
            R.drawable.ic_mountain, R.drawable.ic_hands, R.drawable.ic_level_all_clear};
    private final int[] stepTitles = {R.string.guide_step1_title, R.string.guide_step2_title, R.string.guide_step3_title,
            R.string.guide_step4_title, R.string.guide_step5_title, R.string.guide_step6_title};
    private final int[] stepTexts = {R.string.guide_step1_text, R.string.guide_step2_text, R.string.guide_step3_text,
            R.string.guide_step4_text, R.string.guide_step5_text, R.string.guide_step6_text};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccessGuard.check(this, Permission.RECEIVE_ALERTS)) {
            return;
        }
        setContentView(R.layout.activity_safety_guide);
        TopBarHelper.setup(this, R.string.guide_title);

        LinearLayout layoutGuideSteps = findViewById(R.id.layoutGuideSteps);
        MaterialButton buttonGuidePractice = findViewById(R.id.buttonGuidePractice);

        for (int i = 0; i < stepTitles.length; i++) {
            View stepView = LayoutInflater.from(this).inflate(R.layout.item_guide_step, layoutGuideSteps, false);
            ImageView imageGuideStep = stepView.findViewById(R.id.imageGuideStep);
            TextView textGuideStepTitle = stepView.findViewById(R.id.textGuideStepTitle);
            TextView textGuideStepText = stepView.findViewById(R.id.textGuideStepText);
            imageGuideStep.setImageResource(stepIcons[i]);
            textGuideStepTitle.setText(stepTitles[i]);
            textGuideStepText.setText(stepTexts[i]);
            layoutGuideSteps.addView(stepView);
        }

        buttonGuidePractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(CommunityAlertHelper.makeFullScreenIntent(SafetyGuideActivity.this, -1, true));
            }
        });
    }
}
