package com.example.cryonix.common;

import android.app.Activity;
import android.content.Intent;

import com.example.cryonix.community.OfflineNoDataActivity;
import com.example.cryonix.data.VillageDataStore;
import com.example.cryonix.data.VillagePack;

/**
 * Every community screen loads its data the same way:
 * fresh pack when online, saved pack when offline, or the "No village data yet" screen.
 */
public class CommunityPackLoader {

    public static VillagePack load(Activity activity) {
        String villageName = new SessionManager(activity).getUserVillage();
        VillagePack pack = VillageDataStore.getPack(activity, villageName);

        if (pack == null) {
            activity.startActivity(new Intent(activity, OfflineNoDataActivity.class));
            activity.finish();
            return null;
        }
        OfflineBannerHelper.show(activity, pack);
        return pack;
    }

    /** "650 m" or "1.2 km" */
    public static String formatDistance(int metres) {
        if (metres < 1000) {
            return metres + " m";
        }
        return String.format(java.util.Locale.ENGLISH, "%.1f km", metres / 1000.0);
    }
}
