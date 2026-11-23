package com.example.cryonix.data.model;

/**
 * One item in the staff Notifications screen (DR-04).
 * itemId is the lake id, node id or alert id that the notice opens.
 */
public class StaffNotice {

    public static final String TYPE_LAKE_FLAGGED = "lake_flagged";
    public static final String TYPE_NODE_SILENT = "node_silent";
    public static final String TYPE_ALERT_SENT = "alert_sent";

    private final String type;
    private final String title;
    private final String text;
    private final String time;
    private final String itemId;
    private final String valley;
    private boolean read = false;

    public StaffNotice(String type, String title, String text, String time, String itemId, String valley) {
        this.type = type;
        this.title = title;
        this.text = text;
        this.time = time;
        this.itemId = itemId;
        this.valley = valley;
    }

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getTime() { return time; }
    public String getItemId() { return itemId; }
    public String getValley() { return valley; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
