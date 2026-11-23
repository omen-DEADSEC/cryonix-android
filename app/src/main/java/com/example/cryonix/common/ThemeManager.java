package com.example.cryonix.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.cryonix.R;

/**
 * Light / dark theme (DR-06).
 * The choice is kept in its own preferences file, so it is remembered even after sign out.
 */
public class ThemeManager {

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    private static final String PREFS_NAME = "cryonix_theme";
    private static final String KEY_THEME = "theme_choice";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static int getSavedTheme(Context context) {
        return getPrefs(context).getInt(KEY_THEME, THEME_SYSTEM);
    }

    /** Called from CryonixApp when the app starts. */
    public static void applySavedTheme(Context context) {
        applyTheme(getSavedTheme(context));
    }

    /** Saves the choice and switches now. Open screens redraw themselves in the new theme. */
    public static void saveAndApply(Context context, int themeChoice) {
        getPrefs(context).edit().putInt(KEY_THEME, themeChoice).apply();
        applyTheme(themeChoice);
    }

    /** Quick toggle used by the moon/sun button on the staff home. */
    public static void toggleLightDark(Context context) {
        if (isDarkNow(context)) {
            saveAndApply(context, THEME_LIGHT);
        } else {
            saveAndApply(context, THEME_DARK);
        }
    }

    public static boolean isDarkNow(Context context) {
        int nightBits = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightBits == Configuration.UI_MODE_NIGHT_YES;
    }

    /** Text such as "Dark" for the Settings button. */
    public static String getThemeName(Context context, int themeChoice) {
        String[] names = context.getResources().getStringArray(R.array.theme_choices);
        return names[themeChoice];
    }

    private static void applyTheme(int themeChoice) {
        if (themeChoice == THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (themeChoice == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
