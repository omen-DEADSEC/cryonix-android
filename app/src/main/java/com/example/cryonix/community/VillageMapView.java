package com.example.cryonix.community;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.cryonix.R;
import com.example.cryonix.data.model.Shelter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple drawn map of the village that works with NO internet (CA-04, CA-05, CA-06).
 * It shows: river, flood zone (red), the village, shelters (squares) and one walking route.
 *
 * Positions are 0.0 to 1.0 of the view size, so the same data fits any screen.
 * TODO: when the map tiles and the flood model (M2) are ready, replace this drawing with a
 * real offline map (for example MapLibre with saved OpenStreetMap tiles) and real flood polygons.
 */
public class VillageMapView extends View {

    /** Called when the user taps a shelter square. */
    public interface OnShelterTapListener {
        void onShelterTapped(Shelter shelter);
    }

    private final List<Shelter> shelterList = new ArrayList<>();
    private Shelter routeShelter = null;
    private OnShelterTapListener shelterTapListener;

    private final Paint paintRiver = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintFloodZone = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintVillage = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintShelter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintShelterFull = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRoute = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintBackground = new Paint();
    private final Paint paintContour = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Village centre on the map
    private static final float VILLAGE_X = 0.50f;
    private static final float VILLAGE_Y = 0.52f;

    public VillageMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;

        paintBackground.setColor(ContextCompat.getColor(context, R.color.color_surface));

        paintContour.setStyle(Paint.Style.STROKE);
        paintContour.setStrokeWidth(1 * density);
        paintContour.setColor(ContextCompat.getColor(context, R.color.color_border));

        paintRiver.setStyle(Paint.Style.STROKE);
        paintRiver.setStrokeWidth(6 * density);
        paintRiver.setStrokeCap(Paint.Cap.ROUND);
        paintRiver.setColor(0xFF38A3D6);

        paintFloodZone.setStyle(Paint.Style.FILL);
        paintFloodZone.setColor(ContextCompat.getColor(context, R.color.color_level_warning));
        paintFloodZone.setAlpha(60);

        paintVillage.setStyle(Paint.Style.FILL);
        paintVillage.setColor(ContextCompat.getColor(context, R.color.color_text_primary));

        paintShelter.setStyle(Paint.Style.FILL);
        paintShelter.setColor(ContextCompat.getColor(context, R.color.color_success));

        paintShelterFull.setStyle(Paint.Style.FILL);
        paintShelterFull.setColor(ContextCompat.getColor(context, R.color.color_text_secondary));

        paintRoute.setStyle(Paint.Style.STROKE);
        paintRoute.setStrokeWidth(4 * density);
        paintRoute.setStrokeCap(Paint.Cap.ROUND);
        paintRoute.setStrokeJoin(Paint.Join.ROUND);
        paintRoute.setColor(ContextCompat.getColor(context, R.color.color_primary));
        paintRoute.setPathEffect(new DashPathEffect(new float[]{12 * density, 8 * density}, 0));

        paintText.setColor(ContextCompat.getColor(context, R.color.color_text_primary));
        paintText.setTextSize(12 * density);
    }

    public void setShelters(List<Shelter> shelters) {
        shelterList.clear();
        shelterList.addAll(shelters);
        invalidate();
    }

    /** Draw the route to this shelter (null = no route). */
    public void setRouteShelter(Shelter shelter) {
        routeShelter = shelter;
        invalidate();
    }

    public void setOnShelterTapListener(OnShelterTapListener listener) {
        shelterTapListener = listener;
    }

    // River runs from bottom-left to the right; this gives its height at a position x
    private float riverY(float x) {
        return 0.84f - 0.14f * x;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float density = getResources().getDisplayMetrics().density;

        canvas.drawRect(0, 0, w, h, paintBackground);

        // A few hill lines so it looks like a valley
        for (int i = 1; i <= 3; i++) {
            Path contour = new Path();
            contour.moveTo(0, h * (0.10f * i));
            contour.quadTo(w * 0.5f, h * (0.10f * i + 0.08f), w, h * (0.10f * i - 0.02f));
            canvas.drawPath(contour, paintContour);
        }

        // Flood zone: band above the river
        Path floodPath = new Path();
        floodPath.moveTo(0, h * (riverY(0) - 0.14f));
        floodPath.lineTo(w, h * (riverY(1) - 0.14f));
        floodPath.lineTo(w, h * (riverY(1) + 0.08f));
        floodPath.lineTo(0, h * (riverY(0) + 0.08f));
        floodPath.close();
        canvas.drawPath(floodPath, paintFloodZone);

        // River
        canvas.drawLine(0, h * riverY(0), w, h * riverY(1), paintRiver);

        // Route
        if (routeShelter != null) {
            float[] points = routeShelter.getRoutePoints();
            Path routePath = new Path();
            routePath.moveTo(w * points[0], h * points[1]);
            for (int i = 2; i + 1 < points.length; i += 2) {
                routePath.lineTo(w * points[i], h * points[i + 1]);
            }
            canvas.drawPath(routePath, paintRoute);
        }

        // Village (a few small houses around the centre)
        float house = 6 * density;
        float[][] houseOffsets = {{0, 0}, {-0.06f, 0.03f}, {0.05f, 0.04f}, {-0.02f, -0.04f}, {0.08f, -0.01f}};
        for (float[] offset : houseOffsets) {
            float cx = w * (VILLAGE_X + offset[0]);
            float cy = h * (VILLAGE_Y + offset[1]);
            canvas.drawRect(cx - house, cy - house, cx + house, cy + house, paintVillage);
        }
        canvas.drawText(getContext().getString(R.string.map_label_village), w * VILLAGE_X + 14 * density,
                h * VILLAGE_Y + 22 * density, paintText);

        // Shelters
        float size = 11 * density;
        for (Shelter shelter : shelterList) {
            float cx = w * shelter.getMapX();
            float cy = h * shelter.getMapY();
            Paint paint = shelter.isFull() ? paintShelterFull : paintShelter;
            canvas.drawRoundRect(new RectF(cx - size, cy - size, cx + size, cy + size), 4 * density, 4 * density, paint);
            canvas.drawText(shortName(shelter.getName()), cx - size, cy + size + 14 * density, paintText);
        }
    }

    /** "Badswat high school" -> "High school" so labels fit on the map. */
    private String shortName(String shelterName) {
        String[] words = shelterName.split(" ", 2);
        if (words.length < 2) {
            return shelterName;
        }
        String rest = words[1];
        return rest.substring(0, 1).toUpperCase() + rest.substring(1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && shelterTapListener != null) {
            float touchRadius = 32 * getResources().getDisplayMetrics().density;
            for (Shelter shelter : shelterList) {
                float dx = event.getX() - getWidth() * shelter.getMapX();
                float dy = event.getY() - getHeight() * shelter.getMapY();
                if (Math.sqrt(dx * dx + dy * dy) < touchRadius) {
                    performClick();
                    shelterTapListener.onShelterTapped(shelter);
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
