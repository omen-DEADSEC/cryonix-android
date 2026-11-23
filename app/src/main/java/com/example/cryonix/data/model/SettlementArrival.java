package com.example.cryonix.data.model;

import com.example.cryonix.R;

/** When the flood front reaches one village (FS-03). Always a range, because it is a model estimate. */
public class SettlementArrival {

    private final String village;
    private final double distanceKm;      // along the river from the lake
    private final int earliestMinutes;
    private final int latestMinutes;
    private final int population;

    public SettlementArrival(String village, double distanceKm, int earliestMinutes, int latestMinutes, int population) {
        this.village = village;
        this.distanceKm = distanceKm;
        this.earliestMinutes = earliestMinutes;
        this.latestMinutes = latestMinutes;
        this.population = population;
    }

    public String getVillage() { return village; }
    public double getDistanceKm() { return distanceKm; }
    public int getEarliestMinutes() { return earliestMinutes; }
    public int getLatestMinutes() { return latestMinutes; }
    public int getPopulation() { return population; }

    /** Colour band by the EARLIEST time (the safe side): under 30, 30-60, 60-120, over 120 minutes. */
    public int getBandColorId() {
        if (earliestMinutes < 30) return R.color.color_level_warning;
        if (earliestMinutes < 60) return R.color.color_level_watch;
        if (earliestMinutes < 120) return R.color.color_level_advisory;
        return R.color.color_level_all_clear;
    }
}
