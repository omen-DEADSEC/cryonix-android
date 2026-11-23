package com.example.cryonix.common;

/**
 * The four roles of Cryonix.
 * What each role is allowed to do is listed in Permission.java.
 */
public enum UserRole {

    SYSTEM_ADMIN("System administrator"),
    AUTHORITY_OFFICER("Authority officer"),
    ORGANISATION_STAFF("Organisation staff"),
    COMMUNITY_MEMBER("Community member");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /** Name shown on the screen, for example "Authority officer". */
    public String getDisplayName() {
        return displayName;
    }

    /** Staff roles sign in with email and password and see the staff side of the app. */
    public boolean isStaff() {
        return this != COMMUNITY_MEMBER;
    }

    /** Shortcut: can this role do the given action? */
    public boolean can(Permission permission) {
        return permission.isAllowedFor(this);
    }

    /** The three roles a staff account can have (used in role pickers). */
    public static UserRole[] staffRoles() {
        return new UserRole[]{SYSTEM_ADMIN, AUTHORITY_OFFICER, ORGANISATION_STAFF};
    }

    /** Convert the saved text back into a role. Returns null if nothing is saved. */
    public static UserRole fromText(String roleText) {
        if (roleText == null) {
            return null;
        }
        for (UserRole role : UserRole.values()) {
            if (role.name().equals(roleText)) {
                return role;
            }
        }
        return null;
    }
}
