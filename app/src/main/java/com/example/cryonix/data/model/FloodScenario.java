package com.example.cryonix.data.model;

import java.util.List;

/**
 * One flood scenario of a lake (FS-01): small, medium or worst case.
 * DEMO: numbers are worked out from the lake area. The real ones come from the flood model (M2:
 * r.avaflow for debris flows, HEC-RAS 2D for water floods).
 */
public class FloodScenario {

    public static final String TYPE_SMALL = "Small";
    public static final String TYPE_MEDIUM = "Medium";
    public static final String TYPE_WORST = "Worst case";

    private final String lakeId;
    private final String type;
    private final double releaseMillionCubicMetres;
    private final int peakFlowCubicMetresPerSecond;
    private final String modelName;
    private final String runDate;
    private final List<SettlementArrival> arrivals;   // nearest village first

    public FloodScenario(String lakeId, String type, double releaseMillionCubicMetres, int peakFlowCubicMetresPerSecond,
                         String modelName, String runDate, List<SettlementArrival> arrivals) {
        this.lakeId = lakeId;
        this.type = type;
        this.releaseMillionCubicMetres = releaseMillionCubicMetres;
        this.peakFlowCubicMetresPerSecond = peakFlowCubicMetresPerSecond;
        this.modelName = modelName;
        this.runDate = runDate;
        this.arrivals = arrivals;
    }

    public String getLakeId() { return lakeId; }
    public String getType() { return type; }
    public double getReleaseMillionCubicMetres() { return releaseMillionCubicMetres; }
    public int getPeakFlowCubicMetresPerSecond() { return peakFlowCubicMetresPerSecond; }
    public String getModelName() { return modelName; }
    public String getRunDate() { return runDate; }
    public List<SettlementArrival> getArrivals() { return arrivals; }
}
