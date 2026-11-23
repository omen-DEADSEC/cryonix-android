package com.example.cryonix.common;

/**
 * Every action in Cryonix that needs a permission.
 * The roles that are allowed to do each action are listed here,
 * so the whole role-based access table is in ONE place.
 * (Screen AC-09 "Roles and permissions" shows this same table.)
 */
public enum Permission {

    //            label shown in the app            admin  officer staff  community
    VIEW_LAKES("View lakes and their status",        true,  true,   true,  false),
    REVIEW_LAKES("Review flagged lakes",             true,  true,   false, false),
    VIEW_FLOOD_SCENARIOS("View flood scenarios",     true,  true,   true,  false),
    PLAN_EVACUATION("Plan and approve evacuation",   true,  true,   false, false),
    CREATE_ALERTS("Create alerts",                   true,  true,   false, false),
    CANCEL_ALERTS("Cancel alerts",                   true,  true,   false, false),
    VIEW_ALERTS("View alerts and delivery status",   true,  true,   true,  false),
    MANAGE_SHELTERS("Manage shelters",               true,  false,  true,  false),
    VIEW_REPORTS("View reports",                     true,  true,   true,  false),
    MANAGE_USERS("Manage users and roles",           true,  false,  false, false),
    MANAGE_ORGANISATIONS("Manage organisations",     true,  false,  false, false),
    VIEW_AUDIT_LOG("Read the audit log",             true,  false,  false, false),
    VIEW_NETWORK_HEALTH("See village node health",   true,  true,   false, false),
    CHANGE_SETTINGS("Change system settings",        true,  false,  false, false),
    RECEIVE_ALERTS("Receive alerts for my village",  false, false,  false, true),
    SEE_ROUTE_AND_SHELTER("See my route and shelter",false, false,  false, true);

    private final String label;
    private final boolean adminAllowed;
    private final boolean officerAllowed;
    private final boolean staffAllowed;
    private final boolean communityAllowed;

    Permission(String label, boolean adminAllowed, boolean officerAllowed,
               boolean staffAllowed, boolean communityAllowed) {
        this.label = label;
        this.adminAllowed = adminAllowed;
        this.officerAllowed = officerAllowed;
        this.staffAllowed = staffAllowed;
        this.communityAllowed = communityAllowed;
    }

    public String getLabel() {
        return label;
    }

    /** Is this role allowed to do this action? */
    public boolean isAllowedFor(UserRole role) {
        if (role == null) {
            return false;
        }
        switch (role) {
            case SYSTEM_ADMIN:
                return adminAllowed;
            case AUTHORITY_OFFICER:
                return officerAllowed;
            case ORGANISATION_STAFF:
                return staffAllowed;
            case COMMUNITY_MEMBER:
                return communityAllowed;
            default:
                return false;
        }
    }
}
