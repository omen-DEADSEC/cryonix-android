package com.example.cryonix.common;

import android.app.Activity;
import android.content.Intent;

import com.example.cryonix.auth.ChooseRoleActivity;
import com.example.cryonix.community.CommunityHomeActivity;
import com.example.cryonix.onboarding.OnboardingActivity;
import com.example.cryonix.staff.StaffHomeActivity;

/**
 * Decides which screen to open next.
 * All role-based navigation is kept in this one file, so it is easy to change later.
 */
public class ScreenRouter {

    /** Called by the splash screen when the app starts. */
    public static void openStartScreen(Activity activity) {
        SessionManager sessionManager = new SessionManager(activity);

        if (!sessionManager.isOnboardingDone()) {
            openScreen(activity, OnboardingActivity.class);          // A02
        } else if (!sessionManager.isLoggedIn()) {
            openChooseRole(activity);                                // A03
        } else {
            openHomeForRole(activity, sessionManager.getUserRole());
        }
    }

    /** A03 Choose role. Clears the back stack (used after onboarding and sign out). */
    public static void openChooseRole(Activity activity) {
        openAsNewStart(activity, ChooseRoleActivity.class);
    }

    /** Opens the correct home screen for each role. */
    public static void openHomeForRole(Activity activity, UserRole role) {
        if (role == null) {
            openChooseRole(activity);
        } else if (role == UserRole.COMMUNITY_MEMBER) {
            openAsNewStart(activity, CommunityHomeActivity.class);   // A08
        } else {
            // All three staff roles use the staff home; it shows a menu for each role
            openAsNewStart(activity, StaffHomeActivity.class);       // A20
        }
    }

    /** Signs the user out and goes back to Choose role. */
    public static void signOut(Activity activity) {
        SessionManager sessionManager = new SessionManager(activity);
        String who = sessionManager.getUserName();
        if (who.isEmpty()) {
            who = sessionManager.getUserPhone();
        }
        com.example.cryonix.data.FakeDatabase.addAuditEntry(who, "Signed out");
        sessionManager.logout();
        openChooseRole(activity);
    }

    /** Open a screen and close the current one. */
    private static void openScreen(Activity activity, Class<?> screenClass) {
        Intent intent = new Intent(activity, screenClass);
        activity.startActivity(intent);
        activity.finish();
    }

    /** Open a screen as the only screen (the back button will not go to old screens). */
    private static void openAsNewStart(Activity activity, Class<?> screenClass) {
        Intent intent = new Intent(activity, screenClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
