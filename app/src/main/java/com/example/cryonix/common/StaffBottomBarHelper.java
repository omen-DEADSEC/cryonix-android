package com.example.cryonix.common;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.alerts.AlertsListActivity;
import com.example.cryonix.lakes.LakesListActivity;
import com.example.cryonix.staff.StaffMoreActivity;

/**
 * Bottom bar of the staff app: Home, Lakes, Alerts, More (from the design).
 * Home always stays at the bottom of the back stack, so "Back" from any tab goes to Home.
 */
public class StaffBottomBarHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_LAKES = 1;
    public static final int TAB_ALERTS = 2;
    public static final int TAB_MORE = 3;

    public static void setup(final Activity activity, final int currentTab) {
        LinearLayout tabStaffHome = activity.findViewById(R.id.tabStaffHome);
        LinearLayout tabStaffLakes = activity.findViewById(R.id.tabStaffLakes);
        LinearLayout tabStaffAlerts = activity.findViewById(R.id.tabStaffAlerts);
        LinearLayout tabStaffMore = activity.findViewById(R.id.tabStaffMore);

        highlight(activity, R.id.imageTabStaffHome, R.id.textTabStaffHome, currentTab == TAB_HOME);
        highlight(activity, R.id.imageTabStaffLakes, R.id.textTabStaffLakes, currentTab == TAB_LAKES);
        highlight(activity, R.id.imageTabStaffAlerts, R.id.textTabStaffAlerts, currentTab == TAB_ALERTS);
        highlight(activity, R.id.imageTabStaffMore, R.id.textTabStaffMore, currentTab == TAB_MORE);

        tabStaffHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTab(activity, currentTab, TAB_HOME);
            }
        });
        tabStaffLakes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTab(activity, currentTab, TAB_LAKES);
            }
        });
        tabStaffAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTab(activity, currentTab, TAB_ALERTS);
            }
        });
        tabStaffMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTab(activity, currentTab, TAB_MORE);
            }
        });
    }

    private static void highlight(Activity activity, int imageId, int textId, boolean isSelected) {
        int colorId = isSelected ? R.color.color_primary : R.color.color_text_secondary;
        int color = ContextCompat.getColor(activity, colorId);
        ImageView imageView = activity.findViewById(imageId);
        TextView textView = activity.findViewById(textId);
        imageView.setColorFilter(color);
        textView.setTextColor(color);
    }

    private static void openTab(Activity activity, int currentTab, int newTab) {
        if (newTab == currentTab) {
            return;
        }

        if (newTab != TAB_HOME) {
            Class<?> screenClass;
            if (newTab == TAB_LAKES) {
                screenClass = LakesListActivity.class;
            } else if (newTab == TAB_ALERTS) {
                screenClass = AlertsListActivity.class;
            } else {
                screenClass = StaffMoreActivity.class;
            }
            activity.startActivity(new Intent(activity, screenClass));
        }

        // Close the current tab unless it is Home (Home stays under the other tabs)
        if (currentTab != TAB_HOME) {
            activity.finish();
        }
        activity.overridePendingTransition(0, 0);
    }
}
