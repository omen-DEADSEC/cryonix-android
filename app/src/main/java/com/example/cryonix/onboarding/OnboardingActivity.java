package com.example.cryonix.onboarding;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.ScreenRouter;
import com.example.cryonix.common.SessionManager;
import com.google.android.material.button.MaterialButton;

/**
 * A02 Onboarding screen.
 * Three short pages explain the app. "Next" moves to the next page,
 * "Skip" or "Get started" (last page) goes to the Choose role screen (A03).
 * The onboarding is shown only once (saved in SessionManager).
 */
public class OnboardingActivity extends AppCompatActivity {

    // Data of the 3 pages (picture, title, text). Change them here if needed.
    private final int[] pageIcons = {
            R.drawable.ic_alert_bell,
            R.drawable.ic_route,
            R.drawable.ic_offline_map
    };
    private final int[] pageTitles = {
            R.string.onboarding_page1_title,
            R.string.onboarding_page2_title,
            R.string.onboarding_page3_title
    };
    private final int[] pageTexts = {
            R.string.onboarding_page1_text,
            R.string.onboarding_page2_text,
            R.string.onboarding_page3_text
    };

    private int currentPage = 0;

    private ImageView imageOnboardingIcon;
    private TextView textOnboardingTitle;
    private TextView textOnboardingDescription;
    private View[] pageDots;
    private MaterialButton buttonOnboardingNext;
    private TextView textOnboardingSkip;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        sessionManager = new SessionManager(this);

        // Find the views
        imageOnboardingIcon = findViewById(R.id.imageOnboardingIcon);
        textOnboardingTitle = findViewById(R.id.textOnboardingTitle);
        textOnboardingDescription = findViewById(R.id.textOnboardingDescription);
        buttonOnboardingNext = findViewById(R.id.buttonOnboardingNext);
        textOnboardingSkip = findViewById(R.id.textOnboardingSkip);
        pageDots = new View[]{
                findViewById(R.id.viewOnboardingDot1),
                findViewById(R.id.viewOnboardingDot2),
                findViewById(R.id.viewOnboardingDot3)
        };

        // Button clicks
        buttonOnboardingNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextClicked();
            }
        });

        textOnboardingSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishOnboarding();
            }
        });

        showPage(currentPage);
    }

    private void onNextClicked() {
        if (currentPage < pageTitles.length - 1) {
            currentPage = currentPage + 1;
            showPage(currentPage);
        } else {
            // Last page: "Get started"
            finishOnboarding();
        }
    }

    /** Puts the picture, title and text of one page on the screen. */
    private void showPage(int pageNumber) {
        imageOnboardingIcon.setImageResource(pageIcons[pageNumber]);
        textOnboardingTitle.setText(pageTitles[pageNumber]);
        textOnboardingDescription.setText(pageTexts[pageNumber]);

        // On the last page the button says "Get started" and Skip is hidden
        boolean isLastPage = pageNumber == pageTitles.length - 1;
        if (isLastPage) {
            buttonOnboardingNext.setText(R.string.onboarding_button_get_started);
            textOnboardingSkip.setVisibility(View.INVISIBLE);
        } else {
            buttonOnboardingNext.setText(R.string.onboarding_button_next);
            textOnboardingSkip.setVisibility(View.VISIBLE);
        }

        updateDots(pageNumber);
    }

    /** The dot of the open page is long and blue, the others are small and grey. */
    private void updateDots(int pageNumber) {
        int dotSize = getResources().getDimensionPixelSize(R.dimen.onboarding_dot_size);
        int activeDotWidth = getResources().getDimensionPixelSize(R.dimen.onboarding_dot_active_width);

        for (int i = 0; i < pageDots.length; i++) {
            ViewGroup.LayoutParams params = pageDots[i].getLayoutParams();
            if (i == pageNumber) {
                params.width = activeDotWidth;
                pageDots[i].setBackgroundResource(R.drawable.bg_onboarding_dot_active);
            } else {
                params.width = dotSize;
                pageDots[i].setBackgroundResource(R.drawable.bg_onboarding_dot_inactive);
            }
            pageDots[i].setLayoutParams(params);
        }
    }

    /** Save that onboarding was seen, then go to Choose role (A03). */
    private void finishOnboarding() {
        sessionManager.setOnboardingDone(true);
        ScreenRouter.openChooseRole(this);
        finish();
    }
}
