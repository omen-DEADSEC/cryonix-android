package com.example.cryonix.common;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.community.AlertsHistoryActivity;
import com.example.cryonix.community.CommunityMapActivity;
import com.example.cryonix.community.SheltersActivity;

/**
 * Bottom bar of the community app: Home, Map, Shelters, Alerts (from the design).
 * Works like the staff bar: Home stays under the other tabs, so Back goes to Home.
 */
public class CommunityBottomBarHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_MAP = 1;
    public static final int TAB_SHELTERS = 2;
    public static final int TAB_ALERTS = 3;

    public static void setup(final Activity activity, final int currentTab) {
        int[] tabIds = {R.id.tabCommunityHome, R.id.tabCommunityMap, R.id.tabCommunityShelters, R.id.tabCommunityAlerts};
        int[] imageIds = {R.id.imageTabCommunityHome, R.id.imageTabCommunityMap, R.id.imageTabCommunityShelters, R.id.imageTabCommunityAlerts};
        int[] textIds = {R.id.textTabCommunityHome, R.id.textTabCommunityMap, R.id.textTabCommunityShelters, R.id.textTabCommunityAlerts};

        for (int i = 0; i < tabIds.length; i++) {
            final int tab = i;
            int color = ContextCompat.getColor(activity, tab == currentTab ? R.color.color_primary : R.color.color_text_secondary);
            ImageView imageView = activity.findViewById(imageIds[i]);
            TextView textView = activity.findViewById(textIds[i]);
            imageView.setColorFilter(color);
            textView.setTextColor(color);

            activity.findViewById(tabIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openTab(activity, currentTab, tab);
                }
            });
        }
    }

    private static void openTab(Activity activity, int currentTab, int newTab) {
        if (newTab == currentTab) {
            return;
        }
        if (newTab == TAB_MAP) {
            activity.startActivity(new Intent(activity, CommunityMapActivity.class));
        } else if (newTab == TAB_SHELTERS) {
            activity.startActivity(new Intent(activity, SheltersActivity.class));
        } else if (newTab == TAB_ALERTS) {
            activity.startActivity(new Intent(activity, AlertsHistoryActivity.class));
        }
        if (currentTab != TAB_HOME) {
            activity.finish();
        }
        activity.overridePendingTransition(0, 0);
    }
}
