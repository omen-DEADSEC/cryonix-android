package com.example.cryonix.data;

import com.example.cryonix.data.model.AlertLevel;
import com.example.cryonix.data.model.Shelter;
import com.example.cryonix.data.model.VillageAlert;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the community app needs for ONE village (CA-05).
 * It is downloaded when online and saved on the phone, so map, route, shelters,
 * lake status and past alerts still work without internet.
 */
public class VillagePack {

    public String villageName = "";
    public String syncedAt = "";          // when it was last downloaded, e.g. "23 Nov 2026, 01:20"
    public String lakeName = "";
    public AlertLevel lakeLevel = AlertLevel.BASELINE;
    public String lakeCheckedOn = "";
    public List<VillageAlert> alerts = new ArrayList<>();     // newest first
    public List<Shelter> shelters = new ArrayList<>();        // nearest first

    /** Newest alert, or null if the village never had one. */
    public VillageAlert getLatestAlert() {
        return alerts.isEmpty() ? null : alerts.get(0);
    }

    /** The alert to show on the home screen: newest active alert that is not "All clear". */
    public VillageAlert getActiveAlert() {
        VillageAlert latest = getLatestAlert();
        if (latest != null && latest.isActive() && latest.getLevel() != AlertLevel.ALL_CLEAR) {
            return latest;
        }
        return null;
    }

    /** True when the newest alert is an All clear (CA-07). */
    public boolean isAllClear() {
        VillageAlert latest = getLatestAlert();
        return latest != null && latest.getLevel() == AlertLevel.ALL_CLEAR;
    }

    public Shelter findShelter(String shelterId) {
        for (Shelter shelter : shelters) {
            if (shelter.getId().equals(shelterId)) {
                return shelter;
            }
        }
        return null;
    }

    /** Nearest shelter that still has free places (used for "my safe point"). */
    public Shelter getNearestShelterWithRoom() {
        for (Shelter shelter : shelters) {
            if (!shelter.isFull()) {
                return shelter;
            }
        }
        return shelters.isEmpty() ? null : shelters.get(0);
    }

    public VillageAlert findAlert(int alertId) {
        for (VillageAlert alert : alerts) {
            if (alert.getId() == alertId) {
                return alert;
            }
        }
        return null;
    }
}
