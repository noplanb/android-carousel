package com.example.carousel;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by skamenkovych@codeminders.com on 7/9/2015.
 */
public class OvalSpin extends NineViewGroup.SpinStrategy {

    public double x0;
    public double y0;
    public double angle;
    private double aX;
    private double aY;
    private double bX;
    private double bY;
    public double a;
    public double b;
    public double c;
    private boolean isAnimating;
    private boolean isInited;
    private double currentAngle;
    private double previousAngle;
    private long lastTime;
    private ValueAnimator valueAnimator;

    public OvalSpin(NineViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public double calculateAngle() {
        double startAngle = Math.atan2(aY, aX);
        double endAngle = Math.atan2(bY, bX);
        double diff = normalizedAngle(endAngle - startAngle);
        return diff;
    }

    @Override
    protected float[] calculateOffset(NineViewGroup.Box box, double distance) {
        float[] offset = new float[2];
        View frame = getViewGroup().getFrame(box);
        double x = getInitialPositionX(frame) - x0;
        double y = getInitialPositionY(frame) - y0;
        double newAngle = Math.atan2(y, x) + distance + angle;
        offset[0] = (float) (b*Math.cos(newAngle) - x);
        offset[1] = (float) (a*Math.sin(newAngle) - y);
        return offset;
    }

    @Override
    protected void spin(double startX, double startY, double offsetX, double offsetY) {
        if (getViewGroup() == null || getViewGroup().getChildCount() != 9) {
            return;
        }
        if (offsetX == 0 && offsetY == 0 || !isInited) {
            return;
        }
        aX = startX - x0;
        aY = startY - y0;
        bX = startX + offsetX - x0;
        bY = startY + offsetY - y0;
        applySpin(calculateAngle());
    }

    private void applySpin(double angle) {
        for (int i = 0; i < 8; i++) {
            View v = getViewGroup().getSurroundingFrame(i);
            float[] offset = calculateOffset(getViewGroup().getBox(v), angle);
            v.setTranslationX(offset[0]);
            v.setTranslationY(offset[1]);
        }
        previousAngle = currentAngle;
        currentAngle = angle;
        lastTime = System.nanoTime();
        getViewGroup().notifyUpdateDebug();
    }

    @Override
    protected void cancelSpin() {

    }

    @Override
    protected void initSpin(double startX, double startY, double offsetX, double offsetY) {
        isAnimating = false;
        isInited = false;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        View centerView = getViewGroup().getCenterFrame();
        x0 = getInitialPositionX(centerView);
        y0 = getInitialPositionY(centerView);
        View topLeftView = getViewGroup().getFrame(NineViewGroup.Box.TOP_LEFT);
        double left = getInitialPositionX(topLeftView);
        double top = getInitialPositionY(topLeftView);
        c = y0 - top;
        b = x0 - left;
        a = Math.hypot(b, c);
        angle = angle + currentAngle;
        isInited = true;
    }

    @Override
    protected void finishSpin(double startX, double startY, double offsetX, double offsetY) {
        if (!isInited) {
            return;
        }
        final double angleDiff = normalizedAngle(currentAngle - previousAngle);
        valueAnimator = new ValueAnimator();
        final float startAnimValue = (float) Math.hypot(offsetX, offsetY);
        valueAnimator.setFloatValues(startAnimValue, 0f);
        valueAnimator.setDuration(Math.max((long) (3000 * Math.abs(angleDiff)), 200));
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                applySpin(normalizedAngle(angleDiff * ((Float) animation.getAnimatedValue()) / startAnimValue + currentAngle));
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            boolean isCancelled;

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCancelled) {
                    isInited = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
        isAnimating = true;
    }

    @Override
    public void reset() {
        x0 = y0 = 0;
        angle = 0;
        aX = aY = bX = bY = 0;
        a = b = c = 0;
        isAnimating = false;
        isInited = false;
        if (valueAnimator != null) {
            valueAnimator.end();
        }
        for (int i = 0; i < 8; i++) {
            View v = getViewGroup().getSurroundingFrame(i);
            v.setTranslationX(0);
            v.setTranslationY(0);
        }
    }

    private double getInitialOffsetX(View v) {
        double x = getInitialPositionX(v) - x0;
        double y = getInitialPositionY(v) - y0;
        double newAngle = Math.atan2(y, x);
        return (b*Math.cos(newAngle) - x);
    }

    private double getInitialOffsetY(View v) {
        double x = getInitialPositionX(v) - x0;
        double y = getInitialPositionY(v) - y0;
        double newAngle = Math.atan2(y, x);
        return (a*Math.sin(newAngle) - y);
    }

    private double normalizedAngle(double angle) {
        while (true) {
            if (angle >= Math.PI) {
                angle -= 2 * Math.PI;
            } else if (angle < -Math.PI) {
                angle += 2 * Math.PI;
            } else {
                break;
            }
        }
        return angle;
    }
}
