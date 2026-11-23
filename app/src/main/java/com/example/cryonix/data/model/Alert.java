package com.example.cryonix.data.model;

import java.util.List;

/**
 * An alert sent for a lake to some villages over some channels.
 *
 * DEMO: real delivery numbers will come from the alert engine. Here the numbers are worked out
 * from the seconds since the alert was sent, so the delivery screen looks "live" when you test it.
 */
public class Alert {

    // Channel names (the three Cryonix channels; cell broadcast and voice call are counted apart)
    public static final String CHANNEL_LORA = "LoRa mesh (mosque speakers)";
    public static final String CHANNEL_APP = "Mobile app";
    public static final String CHANNEL_CELL = "Cell broadcast";
    public static final String CHANNEL_VOICE = "Voice call (IVR)";

    /** A village that has not confirmed after this many seconds needs a phone call. */
    public static final int CONFIRM_LIMIT_SECONDS = 120;

    private final int id;
    private final String lakeId;
    private final String lakeName;
    private final String valley;
    private final AlertLevel level;
    private final List<String> villages;
    private final List<String> channels;
    private final String message;
    private final String createdBy;
    private final String timeText;
    private final long sentAtMillis;

    private boolean active = true;
    private long cancelledAtMillis = 0;
    private String cancelReason = "";
    private String closedBy = "";
    private String closedTime = "";

    public Alert(int id, String lakeId, String lakeName, String valley, AlertLevel level,
                 List<String> villages, List<String> channels, String message,
                 String createdBy, String timeText, long sentAtMillis) {
        this.id = id;
        this.lakeId = lakeId;
        this.lakeName = lakeName;
        this.valley = valley;
        this.level = level;
        this.villages = villages;
        this.channels = channels;
        this.message = message;
        this.createdBy = createdBy;
        this.timeText = timeText;
        this.sentAtMillis = sentAtMillis;
    }

    /** Closes the alert (cancelled, or replaced by an All clear) with who and when (AL-05). */
    public void close(String reason, String byName, String time) {
        active = false;
        cancelReason = reason;
        closedBy = byName;
        closedTime = time;
        cancelledAtMillis = System.currentTimeMillis();
    }

    /** Seconds since sending. Stops counting when the alert is cancelled. */
    public int getSecondsSinceSent() {
        long endMillis = active ? System.currentTimeMillis() : cancelledAtMillis;
        return (int) ((endMillis - sentAtMillis) / 1000);
    }

    // ---------- Demo delivery numbers ----------

    /** How many people or devices one village has on this channel (sample numbers). */
    private int getPerVillage(String channel) {
        if (CHANNEL_LORA.equals(channel)) return 1;      // one village node
        if (CHANNEL_APP.equals(channel)) return 40;      // registered app phones
        if (CHANNEL_CELL.equals(channel)) return 150;    // phones in the area
        return 60;                                       // voice call numbers
    }

    public int getChannelTotal(String channel) {
        return getPerVillage(channel) * villages.size();
    }

    /** Sending takes about 8 seconds in the demo. */
    public int getChannelSent(String channel) {
        double part = Math.min(1.0, getSecondsSinceSent() / 8.0);
        return (int) Math.round(getChannelTotal(channel) * part);
    }

    /** About 95% are delivered after 12 seconds (some phones are off). LoRa nodes all answer. */
    public int getChannelDelivered(String channel) {
        double best = CHANNEL_LORA.equals(channel) ? 1.0 : 0.95;
        double part = Math.min(best, getSecondsSinceSent() / 12.0);
        return (int) Math.floor(getChannelTotal(channel) * part);
    }

    /**
     * Each village focal person confirms a few seconds after the last one.
     * In the demo, when there are 3 or more villages the last one never confirms,
     * so the "no confirmation" warning can be tested.
     */
    public boolean isVillageConfirmed(int villageIndex) {
        boolean isLastOfMany = villages.size() >= 3 && villageIndex == villages.size() - 1;
        if (isLastOfMany) {
            return false;
        }
        int confirmAfterSeconds = 6 + villageIndex * 5;
        return getSecondsSinceSent() >= confirmAfterSeconds;
    }

    public int getConfirmedVillageCount() {
        int count = 0;
        for (int i = 0; i < villages.size(); i++) {
            if (isVillageConfirmed(i)) count++;
        }
        return count;
    }

    // ---------- Getters ----------

    public int getId() { return id; }
    public String getLakeId() { return lakeId; }
    public String getLakeName() { return lakeName; }
    public String getValley() { return valley; }
    public AlertLevel getLevel() { return level; }
    public List<String> getVillages() { return villages; }
    public List<String> getChannels() { return channels; }
    public String getMessage() { return message; }
    public String getCreatedBy() { return createdBy; }
    public String getTimeText() { return timeText; }
    public boolean isActive() { return active; }
    public String getCancelReason() { return cancelReason; }
    public String getClosedBy() { return closedBy; }
    public String getClosedTime() { return closedTime; }

    /** Number of people living in the villages of this alert (for the preview, AL-01). */
    public static int countPeople(List<String> villageNames, java.util.Map<String, Integer> populationMap) {
        int total = 0;
        for (String name : villageNames) {
            Integer people = populationMap.get(name);
            total += people == null ? 0 : people;
        }
        return total;
    }
}
