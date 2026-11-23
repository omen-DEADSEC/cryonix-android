package com.example.cryonix.flood;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.data.model.SettlementArrival;

import java.util.ArrayList;
import java.util.List;

/**
 * FS-03: the valley drawn from the lake (top) down the river, coloured by flood arrival time.
 * Each river part takes the colour of the village at its end; villages are dots with their time.
 * TODO: replace with the real arrival-time raster from the flood model on a real map.
 */
public class FloodArrivalMapView extends View {

    private final List<SettlementArrival> arrivalList = new ArrayList<>();
    private String lakeLabel = "";

    private final Paint paintBackground = new Paint();
    private final Paint paintRiver = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLake = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintTextBold = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FloodArrivalMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        paintBackground.setColor(ContextCompat.getColor(context, R.color.color_surface));
        paintRiver.setStyle(Paint.Style.STROKE);
        paintRiver.setStrokeCap(Paint.Cap.ROUND);
        paintRiver.setStrokeWidth(14 * density);
        paintRing.setStyle(Paint.Style.STROKE);
        paintRing.setStrokeWidth(3 * density);
        paintRing.setColor(ContextCompat.getColor(context, R.color.color_surface));
        paintLake.setColor(0xFF2B6F8F);
        paintText.setColor(ContextCompat.getColor(context, R.color.color_text_secondary));
        paintText.setTextSize(11 * density);
        paintTextBold.setColor(ContextCompat.getColor(context, R.color.color_text_primary));
        paintTextBold.setTextSize(13 * density);
        paintTextBold.setFakeBoldText(true);
    }

    public void setArrivals(String lakeName, List<SettlementArrival> arrivals) {
        lakeLabel = lakeName;
        arrivalList.clear();
        arrivalList.addAll(arrivals);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float w = getWidth();
        float h = getHeight();
        canvas.drawRect(0, 0, w, h, paintBackground);

        // Lake at the top left, river goes down in a zig-zag
        float lakeX = w * 0.18f;
        float lakeY = h * 0.12f;
        canvas.drawOval(lakeX - 26 * density, lakeY - 16 * density, lakeX + 26 * density, lakeY + 16 * density, paintLake);
        canvas.drawText(lakeLabel, lakeX + 32 * density, lakeY + 4 * density, paintTextBold);

        if (arrivalList.isEmpty()) {
            return;
        }
        double maxKm = arrivalList.get(arrivalList.size() - 1).getDistanceKm();

        float previousX = lakeX;
        float previousY = lakeY + 16 * density;
        for (int i = 0; i < arrivalList.size(); i++) {
            SettlementArrival arrival = arrivalList.get(i);
            float part = (float) (arrival.getDistanceKm() / maxKm);
            float y = lakeY + 30 * density + part * (h * 0.80f - 30 * density);
            float x = (i % 2 == 0) ? w * 0.62f : w * 0.30f;   // zig-zag left and right
            int color = ContextCompat.getColor(getContext(), arrival.getBandColorId());

            paintRiver.setColor(color);
            paintRiver.setAlpha(170);
            canvas.drawLine(previousX, previousY, x, y, paintRiver);

            paintDot.setColor(color);
            canvas.drawCircle(x, y, 12 * density, paintDot);
            canvas.drawCircle(x, y, 12 * density, paintRing);

            float textX = (i % 2 == 0) ? x - 140 * density : x + 18 * density;
            canvas.drawText(arrival.getVillage(), textX, y - 2 * density, paintTextBold);
            canvas.drawText(getContext().getString(R.string.flood_arrival_range,
                    arrival.getEarliestMinutes(), arrival.getLatestMinutes()), textX, y + 13 * density, paintText);

            previousX = x;
            previousY = y;
        }
    }
}
