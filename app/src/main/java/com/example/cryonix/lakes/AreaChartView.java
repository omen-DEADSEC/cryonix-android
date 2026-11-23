package com.example.cryonix.lakes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.cryonix.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LM-05: line chart of lake area against time, with the past maximum marked (dashed line).
 * Also used for node battery and signal (LR-08): pass 0 as the past maximum to hide that line.
 * Drawn by hand with Canvas, so no chart library is needed.
 */
public class AreaChartView extends View {

    private final List<Double> values = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private double pastMax = 0;

    private final Paint paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintMax = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintMaxText = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AreaChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        int primary = ContextCompat.getColor(context, R.color.color_primary);

        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(2.5f * density);
        paintLine.setStrokeJoin(Paint.Join.ROUND);
        paintLine.setColor(primary);

        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setColor(primary);
        paintFill.setAlpha(30);

        paintDot.setStyle(Paint.Style.FILL);
        paintDot.setColor(primary);

        paintMax.setStyle(Paint.Style.STROKE);
        paintMax.setStrokeWidth(1.5f * density);
        paintMax.setColor(ContextCompat.getColor(context, R.color.color_level_warning));
        paintMax.setPathEffect(new DashPathEffect(new float[]{8 * density, 6 * density}, 0));

        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(1 * density);
        paintGrid.setColor(ContextCompat.getColor(context, R.color.color_border));

        paintText.setColor(ContextCompat.getColor(context, R.color.color_text_secondary));
        paintText.setTextSize(11 * density);

        paintMaxText.setColor(ContextCompat.getColor(context, R.color.color_level_warning));
        paintMaxText.setTextSize(11 * density);
    }

    public void setData(List<String> monthLabels, List<Double> areaValues, double pastMaxArea) {
        labels.clear();
        labels.addAll(monthLabels);
        values.clear();
        values.addAll(areaValues);
        pastMax = pastMaxArea;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.size() < 2) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        float left = 36 * density;
        float right = getWidth() - 8 * density;
        float top = 16 * density;
        float bottom = getHeight() - 22 * density;

        // Y range: a little below the smallest and above the past maximum
        double min = values.get(0);
        for (double value : values) {
            if (value < min) min = value;
        }
        double max = values.get(0);
        for (double value : values) {
            if (value > max) max = value;
        }
        boolean hasMaxLine = pastMax > 0;
        double low;
        double high;
        if (hasMaxLine) {
            low = min * 0.9;
            high = pastMax * 1.08;
        } else {
            // general chart (also works for negative values such as signal in dBm)
            double padding = Math.max(1, (max - min) * 0.15);
            low = min - padding;
            high = max + padding;
        }

        // Grid lines with area labels
        for (int i = 0; i <= 3; i++) {
            float y = bottom - (bottom - top) * i / 3f;
            canvas.drawLine(left, y, right, y, paintGrid);
            double value = low + (high - low) * i / 3.0;
            canvas.drawText(String.format(Locale.ENGLISH, "%.0f", value), 0, y + 4 * density, paintText);
        }

        // Line and filled area under it
        Path linePath = new Path();
        Path fillPath = new Path();
        float stepX = (right - left) / (values.size() - 1);
        for (int i = 0; i < values.size(); i++) {
            float x = left + stepX * i;
            float y = toY(values.get(i), low, high, top, bottom);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, bottom);
            }
            if (i > 0) {
                linePath.lineTo(x, y);
            }
            fillPath.lineTo(x, y);
        }
        fillPath.lineTo(right, bottom);
        fillPath.close();
        canvas.drawPath(fillPath, paintFill);
        canvas.drawPath(linePath, paintLine);

        // Dots and month labels (every second month so they fit)
        for (int i = 0; i < values.size(); i++) {
            float x = left + stepX * i;
            float y = toY(values.get(i), low, high, top, bottom);
            canvas.drawCircle(x, y, (i == values.size() - 1 ? 5 : 3) * density, paintDot);
            if (i % 2 == 1 || i == values.size() - 1) {
                canvas.drawText(labels.get(i), x - 10 * density, getHeight() - 4 * density, paintText);
            }
        }

        if (!hasMaxLine) {
            return;
        }
        // Past maximum
        float maxY = toY(pastMax, low, high, top, bottom);
        canvas.drawLine(left, maxY, right, maxY, paintMax);
        canvas.drawText(getContext().getString(R.string.lake_chart_max,
                String.format(Locale.ENGLISH, "%.1f", pastMax)), left + 4 * density, maxY - 4 * density, paintMaxText);
    }

    private float toY(double value, double low, double high, float top, float bottom) {
        return (float) (bottom - (value - low) / (high - low) * (bottom - top));
    }
}
