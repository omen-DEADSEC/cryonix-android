package com.example.cryonix.data.model;

/** One line of the audit log (AC-10): who did what and when. */
public class AuditEntry {

    private final String time;
    private final String userName;
    private final String action;

    public AuditEntry(String time, String userName, String action) {
        this.time = time;
        this.userName = userName;
        this.action = action;
    }

    public String getTime() { return time; }
    public String getUserName() { return userName; }
    public String getAction() { return action; }
}
