package com.example.cryonix.common;

import android.app.Activity;
import android.widget.Toast;

import com.example.cryonix.R;

/**
 * Role check at the start of a protected screen.
 * Example (in onCreate): if (!AccessGuard.check(this, Permission.MANAGE_USERS)) return;
 */
public class AccessGuard {

    /**
     * Returns true if the signed-in user may open this screen.
     * If not, shows a message and closes the screen.
     */
    public static boolean check(Activity activity, Permission permission) {
        SessionManager sessionManager = new SessionManager(activity);
        UserRole role = sessionManager.getUserRole();

        if (!sessionManager.isLoggedIn() || role == null || !role.can(permission)) {
            Toast.makeText(activity, R.string.message_no_access, Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        return true;
    }
}
