package com.example.cryonix.data.model;

import java.util.ArrayList;
import java.util.List;

/** A glacial lake watched by the system (sample data until the lake model API is ready). */
public class Lake {

    private final String id;
    private final String name;
    private final String valley;
    private final String district;
    private final double areaHectares;
    private final double areaChangePercent;   // change in the last 30 days
    private final int elevationMetres;
    private final String lastImageDate;
    private final List<String> downstreamVillages;

    private AlertLevel level;
    private boolean flagged;
    private String flagReason;
    private int modelConfidencePercent;
    private String lastReviewNote = "";
    private String lastReviewBy = "";
    private String lastReviewTime = "";
    private String lastReviewDecision = "";

    // LM-05: area history (one value per month, oldest first) with month labels
    private List<Double> areaHistory = new ArrayList<>();
    private List<String> historyMonths = new ArrayList<>();
    private String previousImageDate = "";

    // LM-05: notes from the lake inventory
    private String glacierNote = "";
    private String slopeNote = "";
    private String damNote = "";

    // LM-04: position for the lakes map
    private double latitude;
    private double longitude;

    public Lake(String id, String name, String valley, String district, double areaHectares,
                double areaChangePercent, int elevationMetres, String lastImageDate,
                List<String> downstreamVillages, AlertLevel level) {
        this.id = id;
        this.name = name;
        this.valley = valley;
        this.district = district;
        this.areaHectares = areaHectares;
        this.areaChangePercent = areaChangePercent;
        this.elevationMetres = elevationMetres;
        this.lastImageDate = lastImageDate;
        this.downstreamVillages = downstreamVillages;
        this.level = level;
    }

    /** Marks the lake for an officer to review (done by the lake model). */
    public void setFlag(String reason, int confidencePercent) {
        this.flagged = true;
        this.flagReason = reason;
        this.modelConfidencePercent = confidencePercent;
    }

    public void clearFlag() {
        this.flagged = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getValley() { return valley; }
    public String getDistrict() { return district; }
    public double getAreaHectares() { return areaHectares; }
    public double getAreaChangePercent() { return areaChangePercent; }
    public int getElevationMetres() { return elevationMetres; }
    public String getLastImageDate() { return lastImageDate; }
    public List<String> getDownstreamVillages() { return downstreamVillages; }

    public AlertLevel getLevel() { return level; }
    public void setLevel(AlertLevel level) { this.level = level; }

    public boolean isFlagged() { return flagged; }
    public String getFlagReason() { return flagReason; }
    public int getModelConfidencePercent() { return modelConfidencePercent; }

    public String getLastReviewNote() { return lastReviewNote; }
    public void setLastReviewNote(String lastReviewNote) { this.lastReviewNote = lastReviewNote; }

    // ---------- History, images and notes (LM-05) ----------

    public void setHistory(List<String> months, List<Double> areas, String previousImageDate) {
        this.historyMonths = months;
        this.areaHistory = areas;
        this.previousImageDate = previousImageDate;
    }

    public List<Double> getAreaHistory() { return areaHistory; }
    public List<String> getHistoryMonths() { return historyMonths; }
    public String getPreviousImageDate() { return previousImageDate; }

    /** Largest area ever measured (marked on the chart). */
    public double getPastMaxArea() {
        double max = areaHectares;
        for (double area : areaHistory) {
            if (area > max) max = area;
        }
        return max;
    }

    /** Area on the previous image (second last value of the history). */
    public double getPreviousArea() {
        if (areaHistory.size() < 2) {
            return areaHectares;
        }
        return areaHistory.get(areaHistory.size() - 2);
    }

    public void setNotes(String glacierNote, String slopeNote, String damNote) {
        this.glacierNote = glacierNote;
        this.slopeNote = slopeNote;
        this.damNote = damNote;
    }

    public String getGlacierNote() { return glacierNote; }
    public String getSlopeNote() { return slopeNote; }
    public String getDamNote() { return damNote; }

    // ---------- Position (LM-04 map) ----------

    public void setPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // ---------- Last review (LM-07: saved with user and time) ----------

    public void setLastReview(String decision, String note, String byName, String time) {
        this.lastReviewDecision = decision;
        this.lastReviewNote = note;
        this.lastReviewBy = byName;
        this.lastReviewTime = time;
    }

    public String getLastReviewBy() { return lastReviewBy; }
    public String getLastReviewTime() { return lastReviewTime; }
    public String getLastReviewDecision() { return lastReviewDecision; }
}
