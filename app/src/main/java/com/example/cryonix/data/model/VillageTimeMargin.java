package com.example.cryonix.data.model;

import com.example.cryonix.R;

import java.util.List;

/**
 * EV-02: time margin of every house in one village.
 * margin = flood arrival time - (detection + alert + getting ready + walking to safety)
 *   enough time : margin of 10 minutes or more
 *   tight       : 0 to 9 minutes
 *   no time     : below 0 (cannot reach safety before the flood without help)
 */
public class VillageTimeMargin {

    public static final int CLASS_ENOUGH = 0;
    public static final int CLASS_TIGHT = 1;
    public static final int CLASS_NONE = 2;
    public static final int TIGHT_LIMIT_MINUTES = 10;

    private final String village;
    private final int arrivalMinutes;          // earliest arrival of the chosen scenario
    private final List<Integer> houseMargins;  // one value per house, in minutes

    public VillageTimeMargin(String village, int arrivalMinutes, List<Integer> houseMargins) {
        this.village = village;
        this.arrivalMinutes = arrivalMinutes;
        this.houseMargins = houseMargins;
    }

    public static int getClassOf(int marginMinutes) {
        if (marginMinutes >= TIGHT_LIMIT_MINUTES) return CLASS_ENOUGH;
        if (marginMinutes >= 0) return CLASS_TIGHT;
        return CLASS_NONE;
    }

    public static int getColorIdOf(int houseClass) {
        if (houseClass == CLASS_ENOUGH) return R.color.color_level_all_clear;
        if (houseClass == CLASS_TIGHT) return R.color.color_level_advisory;
        return R.color.color_level_warning;
    }

    public int countHouses(int houseClass) {
        int count = 0;
        for (int margin : houseMargins) {
            if (getClassOf(margin) == houseClass) count++;
        }
        return count;
    }

    public String getVillage() { return village; }
    public int getArrivalMinutes() { return arrivalMinutes; }
    public List<Integer> getHouseMargins() { return houseMargins; }
}
