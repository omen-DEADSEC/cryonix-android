package com.example.cryonix.common;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.example.cryonix.R;
import com.example.cryonix.data.VillagePack;

/** Shows the "Offline" label at the top of community screens (CA-05). Needs view_offline_banner in the layout. */
public class OfflineBannerHelper {

    public static void show(Activity activity, VillagePack pack) {
        TextView textOfflineBanner = activity.findViewById(R.id.textOfflineBanner);
        if (textOfflineBanner == null) {
            return;
        }
        if (NetworkHelper.isOnline(activity) || pack == null) {
            textOfflineBanner.setVisibility(View.GONE);
        } else {
            textOfflineBanner.setText(activity.getString(R.string.offline_banner, pack.syncedAt));
            textOfflineBanner.setVisibility(View.VISIBLE);
        }
    }
}
