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

/**
 * LM-05 / LM-07: picture of the lake outline from one satellite image.
 * DEMO: the outline is a drawn shape whose size follows the lake area.
 * TODO: show the real Sentinel-2 image tile with the model outline (M1) on top.
 * When a "compare" area is set, the older outline is drawn dashed for comparison.
 */
public class LakeOutlineView extends View {

    private double areaHectares = 10;
    private double compareAreaHectares = 0;   // 0 = no comparison outline
    private double maxAreaHectares = 20;
    private int seed = 1;                     // makes each lake's shape a little different

    private final Paint paintGround = new Paint();
    private final Paint paintGlacier = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintWater = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCompare = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LakeOutlineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;

        paintGround.setColor(0xFF8A7F72);            // rock colour, same in light and dark (it is a "photo")
        paintGlacier.setColor(0xFFE8F1F7);
        paintWater.setColor(0xFF2B6F8F);
        paintOutline.setStyle(Paint.Style.STROKE);
        paintOutline.setStrokeWidth(2.5f * density);
        paintOutline.setColor(0xFFFFD54F);           // model outline in yellow
        paintCompare.setStyle(Paint.Style.STROKE);
        paintCompare.setStrokeWidth(2 * density);
        paintCompare.setColor(ContextCompat.getColor(context, R.color.white));
        paintCompare.setPathEffect(new DashPathEffect(new float[]{8 * density, 6 * density}, 0));
    }

    public void setLake(double areaHectares, double compareAreaHectares, double maxAreaHectares, int seed) {
        this.areaHectares = areaHectares;
        this.compareAreaHectares = compareAreaHectares;
        this.maxAreaHectares = Math.max(maxAreaHectares, areaHectares);
        this.seed = seed;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        canvas.drawRect(0, 0, w, h, paintGround);

        // Glacier at the top
        Path glacierPath = new Path();
        glacierPath.moveTo(0, 0);
        glacierPath.lineTo(w, 0);
        glacierPath.lineTo(w, h * 0.22f);
        glacierPath.quadTo(w * 0.5f, h * 0.36f, 0, h * 0.20f);
        glacierPath.close();
        canvas.drawPath(glacierPath, paintGlacier);

        Path lakePath = makeLakePath(areaHectares, w, h);
        canvas.drawPath(lakePath, paintWater);
        canvas.drawPath(lakePath, paintOutline);

        if (compareAreaHectares > 0) {
            canvas.drawPath(makeLakePath(compareAreaHectares, w, h), paintCompare);
        }
    }

    /** A rounded blob; its size grows with the square root of the area (area = size x size). */
    private Path makeLakePath(double area, float w, float h) {
        float scale = (float) Math.sqrt(area / maxAreaHectares);
        float cx = w * 0.5f;
        float cy = h * 0.58f;
        float rx = w * 0.36f * scale;
        float ry = h * 0.26f * scale;
        float wobble = 0.10f + (seed % 3) * 0.04f;

        Path path = new Path();
        int points = 12;
        for (int i = 0; i <= points; i++) {
            double angle = 2 * Math.PI * i / points;
            float bump = 1 + (float) (wobble * Math.sin(angle * (2 + seed % 3)));
            float x = cx + (float) (Math.cos(angle) * rx * bump);
            float y = cy + (float) (Math.sin(angle) * ry * bump);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        return path;
    }
}
