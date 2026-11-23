package com.example.cryonix.evacuation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.cryonix.data.model.VillageTimeMargin;

import java.util.ArrayList;
import java.util.List;

/**
 * EV-02: one small square per house, coloured by its class (green enough, yellow tight, red no time).
 * Houses are sorted so the red ones come first and stand out.
 * TODO: on the real evacuation map each square becomes a building footprint (Open Buildings data).
 */
public class HouseMarginView extends View {

    private final List<Integer> margins = new ArrayList<>();
    private final Paint paintHouse = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int COLUMNS = 20;

    public HouseMarginView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMargins(List<Integer> houseMargins) {
        margins.clear();
        margins.addAll(houseMargins);
        // smallest margin first (bubble sort, small lists)
        for (int i = 0; i < margins.size(); i++) {
            for (int j = 0; j < margins.size() - 1 - i; j++) {
                if (margins.get(j) > margins.get(j + 1)) {
                    int temp = margins.get(j);
                    margins.set(j, margins.get(j + 1));
                    margins.set(j + 1, temp);
                }
            }
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float cell = width / (float) COLUMNS;
        int rows = (margins.size() + COLUMNS - 1) / COLUMNS;
        setMeasuredDimension(width, Math.round(rows * cell));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cell = getWidth() / (float) COLUMNS;
        float gap = cell * 0.18f;
        for (int i = 0; i < margins.size(); i++) {
            int row = i / COLUMNS;
            int column = i % COLUMNS;
            int houseClass = VillageTimeMargin.getClassOf(margins.get(i));
            paintHouse.setColor(ContextCompat.getColor(getContext(), VillageTimeMargin.getColorIdOf(houseClass)));
            float left = column * cell + gap / 2;
            float top = row * cell + gap / 2;
            canvas.drawRoundRect(left, top, left + cell - gap, top + cell - gap, gap, gap, paintHouse);
        }
    }
}
