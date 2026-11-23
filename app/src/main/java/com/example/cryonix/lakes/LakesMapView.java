package com.example.cryonix.lakes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.data.model.Lake;

import java.util.ArrayList;
import java.util.List;

/**
 * LM-04: all lakes on a simple map, one coloured dot per lake (colour = level), with the lake ID.
 * Positions come from latitude and longitude, fitted to the view.
 * TODO: replace with a real map (MapLibre / OpenStreetMap) when the map setup task is done.
 */
public class LakesMapView extends View {

    public interface OnLakeTapListener {
        void onLakeTapped(Lake lake);
    }

    private final List<Lake> lakeList = new ArrayList<>();
    private OnLakeTapListener lakeTapListener;

    // Map box that covers Ghizer and Chitral
    private static final double MIN_LAT = 35.80;
    private static final double MAX_LAT = 36.80;
    private static final double MIN_LON = 71.70;
    private static final double MAX_LON = 74.10;

    private final Paint paintBackground = new Paint();
    private final Paint paintDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRegion = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LakesMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        paintBackground.setColor(ContextCompat.getColor(context, R.color.color_surface));
        paintRing.setStyle(Paint.Style.STROKE);
        paintRing.setStrokeWidth(2 * density);
        paintRing.setColor(ContextCompat.getColor(context, R.color.color_surface));
        paintText.setColor(ContextCompat.getColor(context, R.color.color_text_primary));
        paintText.setTextSize(11 * density);
        paintRegion.setColor(ContextCompat.getColor(context, R.color.color_text_secondary));
        paintRegion.setTextSize(13 * density);
        paintRegion.setFakeBoldText(true);
    }

    public void setLakes(List<Lake> lakes) {
        lakeList.clear();
        lakeList.addAll(lakes);
        invalidate();
    }

    public void setOnLakeTapListener(OnLakeTapListener listener) {
        lakeTapListener = listener;
    }

    private float toX(double longitude) {
        return (float) ((longitude - MIN_LON) / (MAX_LON - MIN_LON) * getWidth());
    }

    private float toY(double latitude) {
        return (float) ((MAX_LAT - latitude) / (MAX_LAT - MIN_LAT) * getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        canvas.drawRect(0, 0, getWidth(), getHeight(), paintBackground);

        // District names as a guide
        canvas.drawText(getContext().getString(R.string.lakes_map_chitral), toX(71.85), toY(35.86), paintRegion);
        canvas.drawText(getContext().getString(R.string.lakes_map_ghizer), toX(73.30), toY(35.86), paintRegion);

        for (Lake lake : lakeList) {
            float x = toX(lake.getLongitude());
            float y = toY(lake.getLatitude());
            paintDot.setColor(ContextCompat.getColor(getContext(), lake.getLevel().getColorId()));
            float radius = (lake.isFlagged() ? 11 : 8) * density;
            canvas.drawCircle(x, y, radius, paintDot);
            canvas.drawCircle(x, y, radius, paintRing);
            canvas.drawText(lake.getId(), x + 12 * density, y + 4 * density, paintText);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && lakeTapListener != null) {
            float touchRadius = 28 * getResources().getDisplayMetrics().density;
            for (Lake lake : lakeList) {
                float dx = event.getX() - toX(lake.getLongitude());
                float dy = event.getY() - toY(lake.getLatitude());
                if (Math.sqrt(dx * dx + dy * dy) < touchRadius) {
                    performClick();
                    lakeTapListener.onLakeTapped(lake);
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
