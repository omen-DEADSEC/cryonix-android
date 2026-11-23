package com.example.cryonix;

import android.app.Application;

import com.example.cryonix.common.CommunityAlertHelper;
import com.example.cryonix.common.NotificationHelper;
import com.example.cryonix.common.ThemeManager;

/**
 * Runs once when the app process starts, before any screen.
 * Applies the saved light/dark theme (DR-06) and makes the notification channel (DR-04).
 */
public class CryonixApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
        NotificationHelper.createChannel(this);
        CommunityAlertHelper.createChannel(this);
    }
}
