package com.example.hang.game;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HangmanCanvasView extends View {

    private Paint framePaint;
    private Paint figurePaint;
    private Paint textPaint;
    private Paint bloodPaint;
    private Paint puddlePaint;
    private Paint wrongTextPaint;

    private int wrongGuesses = 0;
    private String atmosphereText = "THEY ARE STILL WAITING.";

    private float drawProgress = 1.0f;
    private ValueAnimator drawAnimator;

    private float swayAngle = 0f;
    private ValueAnimator swayAnimator;

    private final List<BloodDrop> bloodDrops = new ArrayList<>();
    private final List<BloodSplatter> splatters = new ArrayList<>();
    private final Random random = new Random();
    private boolean isBloodFalling = false;
    private float bloodPuddleWidth = 0f;

    private static class BloodDrop {
        float x;
        float y;
        float speed;
        float radius;
        int alpha;

        BloodDrop(float x, float startY, float speed, float radius) {
            this.x = x;
            this.y = startY;
            this.speed = speed;
            this.radius = radius;
            this.alpha = 255;
        }
    }

    private static class BloodSplatter {
        float x;
        float y;
        float dripLength;
        float radius;

        BloodSplatter(float x, float y, float dripLength, float radius) {
            this.x = x;
            this.y = y;
            this.dripLength = dripLength;
            this.radius = radius;
        }
    }

    public HangmanCanvasView(Context context) {
        super(context);
        init();
    }

    public HangmanCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HangmanCanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(Color.parseColor("#FF0000"));
        framePaint.setStrokeWidth(12f);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setShadowLayer(16f, 0, 0, Color.parseColor("#FF0000"));

        figurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        figurePaint.setColor(Color.parseColor("#FF1A1A"));
        figurePaint.setStrokeWidth(10f);
        figurePaint.setStyle(Paint.Style.STROKE);
        figurePaint.setShadowLayer(14f, 0, 0, Color.parseColor("#FF0000"));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#CCCCCC"));
        textPaint.setTextSize(36f);
        textPaint.setLetterSpacing(0.08f);
        textPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        textPaint.setShadowLayer(10f, 0, 0, Color.parseColor("#880000"));

        wrongTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wrongTextPaint.setColor(Color.parseColor("#D31212"));
        wrongTextPaint.setTextSize(48f);
        wrongTextPaint.setLetterSpacing(0.12f);
        wrongTextPaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        wrongTextPaint.setShadowLayer(14f, 0, 0, Color.parseColor("#FF0000"));

        // Light mode: black text with no red glow
        boolean isLight = (getContext().getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                != android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (isLight) {
            textPaint.setColor(Color.BLACK);
            textPaint.clearShadowLayer();

            wrongTextPaint.setColor(Color.BLACK);
            wrongTextPaint.clearShadowLayer();
        }

        bloodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bloodPaint.setColor(Color.parseColor("#E50914"));
        bloodPaint.setStyle(Paint.Style.FILL);
        bloodPaint.setShadowLayer(12f, 0, 0, Color.parseColor("#FF0000"));

        puddlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        puddlePaint.setColor(Color.parseColor("#D31212"));
        puddlePaint.setStyle(Paint.Style.FILL);
        puddlePaint.setShadowLayer(8f, 0, 0, Color.parseColor("#FF0000"));

        startSwayAnimation();
    }

    private void startSwayAnimation() {
        swayAnimator = ValueAnimator.ofFloat(-3f, 3f);
        swayAnimator.setDuration(2000);
        swayAnimator.setRepeatCount(ValueAnimator.INFINITE);
        swayAnimator.setRepeatMode(ValueAnimator.REVERSE);
        swayAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        swayAnimator.addUpdateListener(animation -> {
            swayAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
        swayAnimator.start();
    }

    public void setWrongGuesses(int wrongGuesses) {
        if (wrongGuesses > this.wrongGuesses) {
            vibratePhone(wrongGuesses);
            spawnBloodSplatters();
            setAtmosphereText("WRONG.");
        }

        this.wrongGuesses = wrongGuesses;
        animateStep();

        if (wrongGuesses >= 6) {
            startBloodFallingAnimation();
        } else {
            isBloodFalling = false;
            bloodPuddleWidth = 0f;
        }
    }

    private void vibratePhone(int count) {

        if (!com.example.hang.SoundManager.isVibrationOn(getContext())) {
            return;
        }
        try {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (count >= 6) {
                        // Heavy double-pulse vibration on final blood drip
                        v.vibrate(VibrationEffect.createWaveform(new long[]{0, 250, 100, 350}, -1));
                    } else {
                        // Impact pulse on blood splatter
                        v.vibrate(VibrationEffect.createOneShot(220, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                } else {
                    if (count >= 6) {
                        v.vibrate(new long[]{0, 250, 100, 350}, -1);
                    } else {
                        v.vibrate(220);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void spawnBloodSplatters() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float pad = 40f;
        float baseRight = width - pad;
        float baseBottom = height - pad - 30f;
        float topY = pad + 40f;
        float poleX = pad + 80f;

        // Spawn 3 to 5 scattered blood splatters in the empty area
        int count = 3 + random.nextInt(3);
        int idx = 0;
        while (idx < count) {
            float sx = poleX + random.nextFloat() * (baseRight - poleX - 100f);
            float sy = topY + random.nextFloat() * (baseBottom - topY - 60f);
            float length = 15f + random.nextFloat() * 25f;
            float radius = 4f + random.nextFloat() * 5f;
            splatters.add(new BloodSplatter(sx, sy, length, radius));
            idx++;
        }
    }

    public void setAtmosphereText(String text) {
        this.atmosphereText = text;
        invalidate();
    }

    private void animateStep() {
        if (drawAnimator != null && drawAnimator.isRunning()) {
            drawAnimator.cancel();
        }
        drawAnimator = ValueAnimator.ofFloat(0f, 1f);
        drawAnimator.setDuration(600);
        drawAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        drawAnimator.addUpdateListener(animation -> {
            drawProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        drawAnimator.start();
    }

    private void startBloodFallingAnimation() {
        if (isBloodFalling) return;
        isBloodFalling = true;
        bloodDrops.clear();
        bloodPuddleWidth = 10f;
        invalidate();
    }

    private void updateBloodDrops(float centerBeamX, float bodyTopY, float feetY, float floorY) {
        if (!isBloodFalling) return;

        // Spawn new blood drops continuously from body, hands, and feet
        if (bloodDrops.size() < 35 && random.nextFloat() < 0.6f) {
            float startY = bodyTopY + random.nextFloat() * (feetY - bodyTopY);
            float offsetX = (random.nextFloat() - 0.5f) * 70f;
            float speed = 10f + random.nextFloat() * 14f;
            float radius = 4f + random.nextFloat() * 7f;
            bloodDrops.add(new BloodDrop(centerBeamX + offsetX, startY, speed, radius));
        }

        // Expand blood puddle
        if (bloodPuddleWidth < 120f) {
            bloodPuddleWidth += 0.5f;
        }

        Iterator<BloodDrop> iterator = bloodDrops.iterator();
        while (iterator.hasNext()) {
            BloodDrop drop = iterator.next();
            drop.y += drop.speed;
            if (drop.y >= floorY) {
                drop.y = floorY;
                drop.alpha -= 20;
                if (drop.alpha <= 0) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        float pad = 40f;
        float baseRight = width - pad;
        float baseBottom = height - pad - 30f;
        float topY = pad + 20f;
        float poleX = pad + 60f;
        float beamRight = baseRight - 100f;

        // Draw Gallows Frame
        canvas.drawLine(pad, baseBottom, baseRight, baseBottom, framePaint);
        canvas.drawLine(poleX, baseBottom, poleX, topY, framePaint);
        canvas.drawLine(poleX, topY, beamRight, topY, framePaint);

        float nooseBottomY = topY + 60f;
        canvas.drawLine(beamRight, topY, beamRight, nooseBottomY, framePaint);

        // Draw Scattered Blood Liquid Splatters on Wrong Guesses
        for (BloodSplatter sp : splatters) {
            canvas.drawCircle(sp.x, sp.y, sp.radius, bloodPaint);
            // Dripping liquid line down
            canvas.drawLine(sp.x, sp.y, sp.x, sp.y + sp.dripLength, bloodPaint);
            // Small droplets around splatter
            canvas.drawCircle(sp.x - sp.radius * 1.5f, sp.y - sp.radius, sp.radius * 0.5f, bloodPaint);
            canvas.drawCircle(sp.x + sp.radius * 1.5f, sp.y + sp.dripLength, sp.radius * 0.6f, bloodPaint);
        }

        // Atmosphere / Wrong Text near bottom of frame
        if (atmosphereText != null && !atmosphereText.isEmpty()) {
            if ("WRONG.".equals(atmosphereText)) {
                float textWidth = wrongTextPaint.measureText(atmosphereText);
                float textX = (width - textWidth) / 2f;
                canvas.drawText(atmosphereText, textX, baseBottom - 20f, wrongTextPaint);
            } else {
                canvas.drawText(atmosphereText, poleX + 20f, baseBottom - 20f, textPaint);
            }
        }

        // Apply slight sway rotation centered at noose hook point
        canvas.save();
        canvas.rotate(swayAngle, beamRight, nooseBottomY);

        float headRadius = 28f;
        float headCenterY = nooseBottomY + headRadius;
        float bodyTopY = headCenterY + headRadius;
        float bodyBottomY = bodyTopY + 70f;
        float feetY = bodyBottomY + 55f;

        // 1. First Wrong: Head appears
        if (wrongGuesses >= 1) {
            float p = (wrongGuesses == 1) ? drawProgress : 1.0f;
            figurePaint.setAlpha((int) (255 * p));
            canvas.drawCircle(beamRight, headCenterY, headRadius * p, figurePaint);
            figurePaint.setAlpha(255);
        }

        // 2. Second Wrong: Arms / Hands appear
        if (wrongGuesses >= 2) {
            float p = (wrongGuesses == 2) ? drawProgress : 1.0f;
            figurePaint.setAlpha((int) (255 * p));
            // Left Arm
            canvas.drawLine(beamRight, bodyTopY + 20f, beamRight - 35f * p, bodyTopY + 50f * p, figurePaint);
            // Right Arm
            canvas.drawLine(beamRight, bodyTopY + 20f, beamRight + 35f * p, bodyTopY + 50f * p, figurePaint);
            figurePaint.setAlpha(255);
        }

        // 3. Third Wrong: Full Body (Torso) appears
        if (wrongGuesses >= 3) {
            float p = (wrongGuesses == 3) ? drawProgress : 1.0f;
            figurePaint.setAlpha((int) (255 * p));
            canvas.drawLine(beamRight, bodyTopY, beamRight, bodyTopY + (bodyBottomY - bodyTopY) * p, figurePaint);
            figurePaint.setAlpha(255);
        }

        // 4. Fourth Wrong: Legs appear (Full Hanging Figure)
        if (wrongGuesses >= 4) {
            float p = (wrongGuesses == 4) ? drawProgress : 1.0f;
            figurePaint.setAlpha((int) (255 * p));
            // Left Leg
            canvas.drawLine(beamRight, bodyBottomY, beamRight - 35f * p, bodyBottomY + 55f * p, figurePaint);
            // Right Leg
            canvas.drawLine(beamRight, bodyBottomY, beamRight + 35f * p, bodyBottomY + 55f * p, figurePaint);
            figurePaint.setAlpha(255);
        }

        // 5. Fifth Wrong: Red aura / intense glowing pulse
        if (wrongGuesses >= 5) {
            figurePaint.setShadowLayer(24f, 0, 0, Color.parseColor("#FF0000"));
        } else {
            figurePaint.setShadowLayer(14f, 0, 0, Color.parseColor("#FF0000"));
        }

        canvas.restore();

        // 6. Sixth Wrong / Final Loss: Liquid Blood Falling Animation
        if (wrongGuesses >= 6) {
            updateBloodDrops(beamRight, bodyTopY, feetY, baseBottom);

            // Draw blood puddle on floor
            if (bloodPuddleWidth > 0f) {
                canvas.drawOval(beamRight - bloodPuddleWidth / 2f, baseBottom - 6f, beamRight + bloodPuddleWidth / 2f, baseBottom + 6f, puddlePaint);
            }

            // Draw falling blood drops
            for (BloodDrop drop : bloodDrops) {
                bloodPaint.setAlpha(drop.alpha);
                canvas.drawCircle(drop.x, drop.y, drop.radius, bloodPaint);
                // Draw trailing liquid stream line
                canvas.drawLine(drop.x, drop.y - drop.radius * 2, drop.x, drop.y, bloodPaint);
            }

            // Keep animating blood drops
            if (isBloodFalling) {
                postInvalidateOnAnimation();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (swayAnimator != null) {
            swayAnimator.cancel();
        }
        if (drawAnimator != null) {
            drawAnimator.cancel();
        }
    }
}