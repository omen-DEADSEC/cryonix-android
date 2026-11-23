package com.example.cryonix.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryonix.R;
import com.example.cryonix.common.ScreenRouter;

/**
 * A01 Splash screen.
 * Shows the Cryonix logo, name and tagline for a short time,
 * then opens the next screen. Which screen depends on the user (see ScreenRouter).
 */
public class SplashActivity extends AppCompatActivity {

    // How long the splash screen stays on screen (in milliseconds)
    private static final int SPLASH_DELAY_MS = 2000;

    private Handler splashHandler;
    private Runnable openNextScreenRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashHandler = new Handler(Looper.getMainLooper());

        openNextScreenRunnable = new Runnable() {
            @Override
            public void run() {
                openNextScreen();
            }
        };

        // Wait for SPLASH_DELAY_MS, then move to the next screen
        splashHandler.postDelayed(openNextScreenRunnable, SPLASH_DELAY_MS);
    }

    private void openNextScreen() {
        // ScreenRouter decides: onboarding, choose role, or the home of the user's role
        ScreenRouter.openStartScreen(SplashActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the timer if the user closes the app before it finishes
        if (splashHandler != null) {
            splashHandler.removeCallbacks(openNextScreenRunnable);
        }
    }
}
