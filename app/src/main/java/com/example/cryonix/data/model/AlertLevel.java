package com.example.cryonix.data.model;

import com.example.cryonix.R;

/**
 * The five levels used for lakes and alerts (from the Cryonix design system).
 * A level is ALWAYS shown with colour + icon + label, never colour alone.
 */
public enum AlertLevel {

    BASELINE("Baseline", R.color.color_level_baseline, R.drawable.ic_level_baseline),
    ADVISORY("Advisory", R.color.color_level_advisory, R.drawable.ic_level_advisory),
    WATCH("Watch", R.color.color_level_watch, R.drawable.ic_level_watch),
    WARNING("Warning", R.color.color_level_warning, R.drawable.ic_level_warning),
    ALL_CLEAR("All clear", R.color.color_level_all_clear, R.drawable.ic_level_all_clear);

    private final String label;
    private final int colorId;
    private final int iconId;

    AlertLevel(String label, int colorId, int iconId) {
        this.label = label;
        this.colorId = colorId;
        this.iconId = iconId;
    }

    public String getLabel() { return label; }
    public int getColorId() { return colorId; }
    public int getIconId() { return iconId; }

    /** Lake levels shown in filters and summaries (All clear is only for alerts). */
    public static AlertLevel[] lakeLevels() {
        return new AlertLevel[]{WARNING, WATCH, ADVISORY, BASELINE};
    }

    /** Levels an officer can choose when sending an alert. */
    public static AlertLevel[] alertLevels() {
        return new AlertLevel[]{ADVISORY, WATCH, WARNING, ALL_CLEAR};
    }

    /** Watch and Warning mean people downstream may need to act. */
    public boolean isSerious() {
        return this == WATCH || this == WARNING;
    }
}
