package com.example.cryonix.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A LoRa village node (LR-05, LR-08). Sample data; the real values come from the LoRa gateway (MQTT).
 * Each node reports battery, signal and "I am alive" every REPORT_INTERVAL_MINUTES.
 * A node that misses 3 reports in a row is SILENT.
 */
public class NetworkNode {

    public static final int REPORT_INTERVAL_MINUTES = 15;
    public static final int MISSED_REPORTS_FOR_SILENT = 3;
    public static final int LOW_BATTERY_PERCENT = 20;

    private final String id;
    private final String village;
    private final String valley;
    private final int missedReports;
    private final String lastHeard;
    private final int batteryPercent;
    private final int signalDbm;        // RSSI at the gateway, e.g. -95 dBm (closer to 0 is better)
    private final double latitude;
    private final double longitude;

    public NetworkNode(String id, String village, String valley, int missedReports, String lastHeard,
                       int batteryPercent, int signalDbm, double latitude, double longitude) {
        this.id = id;
        this.village = village;
        this.valley = valley;
        this.missedReports = missedReports;
        this.lastHeard = lastHeard;
        this.batteryPercent = batteryPercent;
        this.signalDbm = signalDbm;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isOnline() {
        return missedReports < MISSED_REPORTS_FOR_SILENT;
    }

    public boolean isBatteryLow() {
        return batteryPercent < LOW_BATTERY_PERCENT;
    }

    /** Simple words for the signal: Good, Fair or Weak. */
    public String getSignalWord() {
        if (signalDbm >= -90) return "Good";
        if (signalDbm >= -110) return "Fair";
        return "Weak";
    }

    // ---------- History for the node page (LR-08) ----------
    // DEMO: 24 hourly values made from today's value; a silent node has no values for its last hours.

    public List<String> getHourLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            labels.add(i == 0 ? "now" : "-" + i + "h");
        }
        return labels;
    }

    public List<Double> getBatteryHistory() {
        List<Double> values = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            // battery was a little higher in the past; solar charge in the middle of the day
            double value = batteryPercent + i * 0.6 + (i > 6 && i < 14 ? -3 : 0);
            values.add(Math.min(100.0, Math.round(value * 10) / 10.0));
        }
        return values;
    }

    public List<Double> getSignalHistory() {
        List<Double> values = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            double value = signalDbm + ((i * 7 + id.hashCode()) % 9) - 4;
            values.add(value);
        }
        return values;
    }

    /** Last messages received from the node, newest first. */
    public List<String> getLastMessages() {
        List<String> messages = new ArrayList<>();
        if (!isOnline()) {
            messages.add(String.format(Locale.ENGLISH, "No report for %d periods (%s)", missedReports, lastHeard));
        }
        messages.add(String.format(Locale.ENGLISH, "%s  •  Heartbeat: battery %d%%, signal %d dBm", lastHeard, batteryPercent, signalDbm));
        messages.add(String.format(Locale.ENGLISH, "%d min before  •  Heartbeat: battery %d%%", REPORT_INTERVAL_MINUTES, Math.min(100, batteryPercent + 1)));
        messages.add("22 Nov 2026, 18:41  •  Alert relayed to mosque speaker, confirmed by focal person");
        messages.add("22 Nov 2026, 06:00  •  Daily self-test passed");
        return messages;
    }

    public String getId() { return id; }
    public String getVillage() { return village; }
    public String getValley() { return valley; }
    public int getMissedReports() { return missedReports; }
    public String getLastHeard() { return lastHeard; }
    public int getBatteryPercent() { return batteryPercent; }
    public int getSignalDbm() { return signalDbm; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
