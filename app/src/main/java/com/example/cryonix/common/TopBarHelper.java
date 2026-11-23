package com.example.cryonix.common;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.cryonix.R;

/**
 * Sets up the top bar (layout/view_top_bar.xml) that most screens include:
 * back arrow on the left and the screen title.
 */
public class TopBarHelper {

    public static void setup(final Activity activity, int titleTextId) {
        TextView textTopBarTitle = activity.findViewById(R.id.textTopBarTitle);
        ImageButton buttonTopBarBack = activity.findViewById(R.id.buttonTopBarBack);

        textTopBarTitle.setText(titleTextId);
        buttonTopBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
            }
        });
    }

    /** For tab screens (Lakes, Alerts, More): title only, no back arrow. */
    public static void setupWithoutBack(Activity activity, int titleTextId) {
        TextView textTopBarTitle = activity.findViewById(R.id.textTopBarTitle);
        ImageButton buttonTopBarBack = activity.findViewById(R.id.buttonTopBarBack);
        textTopBarTitle.setText(titleTextId);
        buttonTopBarBack.setVisibility(View.GONE);
    }
}
