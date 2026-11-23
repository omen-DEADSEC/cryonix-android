package com.example.cryonix.data.model;

import com.example.cryonix.common.UserRole;

/** A staff user (system administrator, authority officer or organisation staff). */
public class StaffAccount {

    private String fullName;
    private String email;
    private String password;      // demo only - the real backend will never store plain passwords
    private String phone;
    private UserRole role;
    private String organisation;
    private boolean active;       // false = account switched off (AC-07)
    private int failedSignInCount; // the account locks after 5 wrong passwords (AC-04)

    public StaffAccount(String fullName, String email, String password, String phone,
                        UserRole role, String organisation) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.organisation = organisation;
        this.active = true;
        this.failedSignInCount = 0;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getOrganisation() { return organisation; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getFailedSignInCount() { return failedSignInCount; }
    public void setFailedSignInCount(int failedSignInCount) { this.failedSignInCount = failedSignInCount; }

    public boolean isLocked() {
        return failedSignInCount >= 5;
    }
}
