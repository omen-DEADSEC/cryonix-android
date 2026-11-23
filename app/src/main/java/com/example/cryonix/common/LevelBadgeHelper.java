package com.example.cryonix.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.cryonix.data.model.AlertLevel;

/**
 * Turns a TextView into a level badge: icon + label + colour, with a thin coloured border.
 * Used on every screen that shows a lake or alert level, so they all look the same.
 */
public class LevelBadgeHelper {

    public static void show(TextView textView, AlertLevel level) {
        Context context = textView.getContext();
        int levelColor = ContextCompat.getColor(context, level.getColorId());

        textView.setText(level.getLabel());
        textView.setTextColor(levelColor);

        // Icon on the left, same colour as the text
        Drawable icon = ContextCompat.getDrawable(context, level.getIconId());
        if (icon != null) {
            icon = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(icon, levelColor);
            int size = dp(context, 16);
            icon.setBounds(0, 0, size, size);
            textView.setCompoundDrawables(icon, null, null, null);
            textView.setCompoundDrawablePadding(dp(context, 4));
        }

        // Rounded border in the level colour
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(context, 12));
        background.setStroke(dp(context, 1), levelColor);
        textView.setBackground(background);
        textView.setPadding(dp(context, 8), dp(context, 4), dp(context, 10), dp(context, 4));
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
