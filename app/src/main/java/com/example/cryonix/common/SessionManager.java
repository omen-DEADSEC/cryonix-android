package com.example.cryonix.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves small things on the phone that the app needs between launches:
 * onboarding seen, who is signed in (role, name, email or phone, village)
 * and the notification choices.
 * (Later the real login token from the backend will also be saved here.)
 */
public class SessionManager {

    private static final String PREFS_NAME = "cryonix_session";

    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_VILLAGE = "user_village";

    // Notification choices (AC-11)
    public static final String KEY_NOTIFY_LAKE_SUMMARY = "notify_lake_summary";
    public static final String KEY_NOTIFY_NODE_HEALTH = "notify_node_health";
    public static final String KEY_NOTIFY_EMAIL = "notify_email";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ---------- Onboarding ----------

    public boolean isOnboardingDone() {
        return preferences.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        preferences.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    // ---------- Sign in / sign out ----------

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    /** Staff sign in (email + password). */
    public void saveStaffLogin(UserRole role, String userName, String email) {
        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USER_ROLE, role.name())
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    /** Community member sign up (phone + village). */
    public void saveCommunityLogin(String phone, String village) {
        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USER_ROLE, UserRole.COMMUNITY_MEMBER.name())
                .putString(KEY_USER_NAME, "")
                .putString(KEY_USER_PHONE, phone)
                .putString(KEY_USER_VILLAGE, village)
                .apply();
    }

    public UserRole getUserRole() {
        return UserRole.fromText(preferences.getString(KEY_USER_ROLE, null));
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public void setUserName(String userName) {
        preferences.edit().putString(KEY_USER_NAME, userName).apply();
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserPhone() {
        return preferences.getString(KEY_USER_PHONE, "");
    }

    public void setUserPhone(String phone) {
        preferences.edit().putString(KEY_USER_PHONE, phone).apply();
    }

    public String getUserVillage() {
        return preferences.getString(KEY_USER_VILLAGE, "");
    }

    public void setUserVillage(String village) {
        preferences.edit().putString(KEY_USER_VILLAGE, village).apply();
    }

    /** Clears the signed-in user but keeps "onboarding done". */
    public void logout() {
        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PHONE)
                .remove(KEY_USER_VILLAGE)
                .apply();
    }

    // ---------- Notification choices ----------

    public boolean getNotificationSetting(String key) {
        return preferences.getBoolean(key, true);
    }

    public void setNotificationSetting(String key, boolean isOn) {
        preferences.edit().putBoolean(key, isOn).apply();
    }
}
