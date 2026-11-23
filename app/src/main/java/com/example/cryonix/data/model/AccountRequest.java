package com.example.cryonix.data.model;

import com.example.cryonix.common.UserRole;

/** A request for a staff account, sent from the website (AC-02) and approved by the admin (AC-06). */
public class AccountRequest {

    private final String fullName;
    private final String email;
    private final String organisation;
    private final UserRole requestedRole;
    private final String reason;

    public AccountRequest(String fullName, String email, String organisation,
                          UserRole requestedRole, String reason) {
        this.fullName = fullName;
        this.email = email;
        this.organisation = organisation;
        this.requestedRole = requestedRole;
        this.reason = reason;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getOrganisation() { return organisation; }
    public UserRole getRequestedRole() { return requestedRole; }
    public String getReason() { return reason; }
}
