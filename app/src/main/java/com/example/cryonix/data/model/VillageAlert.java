package com.example.cryonix.data.model;

/** The part of an alert a community member sees (no staff details). Saved on the phone for offline use. */
public class VillageAlert {

    private final int id;
    private final AlertLevel level;
    private final String lakeName;
    private final String message;
    private final String time;
    private final boolean active;

    public VillageAlert(int id, AlertLevel level, String lakeName, String message, String time, boolean active) {
        this.id = id;
        this.level = level;
        this.lakeName = lakeName;
        this.message = message;
        this.time = time;
        this.active = active;
    }

    public int getId() { return id; }
    public AlertLevel getLevel() { return level; }
    public String getLakeName() { return lakeName; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public boolean isActive() { return active; }
}
