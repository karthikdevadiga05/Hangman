package com.example.hang;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SoulLineGraphView extends View {

    private Paint gridPaint;
    private Paint greenLinePaint;
    private Paint redLinePaint;
    private Paint greenDotPaint;
    private Paint redDotPaint;
    private Paint legendPaint;

    private final Path greenPath = new Path();
    private final Path redPath = new Path();

    private float[] winPoints = new float[]{0, 1, 2, 2, 3, 4, 5};
    private float[] lossPoints = new float[]{0, 2, 3, 5, 5, 6, 6};

    public SoulLineGraphView(Context context) {
        super(context);
        init();
    }

    public SoulLineGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SoulLineGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#2A2A2A"));
        gridPaint.setStrokeWidth(3f);
        gridPaint.setStyle(Paint.Style.STROKE);

        greenLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenLinePaint.setColor(Color.parseColor("#28A745"));
        greenLinePaint.setStrokeWidth(6f);
        greenLinePaint.setStyle(Paint.Style.STROKE);
        greenLinePaint.setShadowLayer(10f, 0, 0, Color.parseColor("#00FF00"));

        redLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redLinePaint.setColor(Color.parseColor("#E50914"));
        redLinePaint.setStrokeWidth(6f);
        redLinePaint.setStyle(Paint.Style.STROKE);
        redLinePaint.setShadowLayer(10f, 0, 0, Color.parseColor("#FF0000"));

        greenDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenDotPaint.setColor(Color.parseColor("#28A745"));
        greenDotPaint.setStyle(Paint.Style.FILL);
        greenDotPaint.setShadowLayer(8f, 0, 0, Color.parseColor("#00FF00"));

        redDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redDotPaint.setColor(Color.parseColor("#E50914"));
        redDotPaint.setStyle(Paint.Style.FILL);
        redDotPaint.setShadowLayer(8f, 0, 0, Color.parseColor("#FF0000"));

        legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPaint.setColor(Color.parseColor("#AAAAAA"));
        legendPaint.setTextSize(26f);
        legendPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
    }

    public void setGraphData(int wins, int losses) {
        winPoints = new float[7];
        lossPoints = new float[7];

        float winStep = wins / 6.0f;
        float lossStep = losses / 6.0f;

        for (int step = 0; step < 7; step++) {
            winPoints[step] = winStep * step;
            lossPoints[step] = lossStep * step;
        }
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        float padLeft = 30f;
        float padRight = width - 30f;
        float padTop = 50f;
        float padBottom = height - 30f;

        // Draw outer border and grid lines
        canvas.drawRect(padLeft, padTop, padRight, padBottom, gridPaint);
        float midY = (padTop + padBottom) / 2f;
        canvas.drawLine(padLeft, midY, padRight, midY, gridPaint);

        // Legends at top
        canvas.drawText("— SAVED (GREEN)", padLeft + 10f, padTop - 15f, legendPaint);
        legendPaint.setColor(Color.parseColor("#E50914"));
        canvas.drawText("— LOST (RED)", padRight - 180f, padTop - 15f, legendPaint);
        legendPaint.setColor(Color.parseColor("#AAAAAA"));

        // Max value for scaling
        float maxVal = 10f;
        for (float v : winPoints) if (v > maxVal) maxVal = v;
        for (float v : lossPoints) if (v > maxVal) maxVal = v;

        float chartWidth = padRight - padLeft;
        float chartHeight = padBottom - padTop;
        float xStep = chartWidth / (winPoints.length - 1);

        // Draw Green Line (Wins/Saved)
        greenPath.reset();
        for (int i = 0; i < winPoints.length; i++) {
            float x = padLeft + i * xStep;
            float y = padBottom - (winPoints[i] / maxVal) * chartHeight;
            if (i == 0) greenPath.moveTo(x, y);
            else greenPath.lineTo(x, y);
        }
        canvas.drawPath(greenPath, greenLinePaint);

        for (int i = 0; i < winPoints.length; i++) {
            float x = padLeft + i * xStep;
            float y = padBottom - (winPoints[i] / maxVal) * chartHeight;
            canvas.drawCircle(x, y, 6f, greenDotPaint);
        }

        // Draw Red Line (Losses/Wrong)
        redPath.reset();
        for (int i = 0; i < lossPoints.length; i++) {
            float x = padLeft + i * xStep;
            float y = padBottom - (lossPoints[i] / maxVal) * chartHeight;
            if (i == 0) redPath.moveTo(x, y);
            else redPath.lineTo(x, y);
        }
        canvas.drawPath(redPath, redLinePaint);

        for (int i = 0; i < lossPoints.length; i++) {
            float x = padLeft + i * xStep;
            float y = padBottom - (lossPoints[i] / maxVal) * chartHeight;
            canvas.drawCircle(x, y, 6f, redDotPaint);
        }
    }
}