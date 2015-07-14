package com.example.carousel;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

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
    private long previousLastTime;
    private long animationLastTime;
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
        previousLastTime = lastTime;
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
        angle = normalizedAngle(angle + currentAngle);
        previousAngle = currentAngle = 0;
        Log.i(TAG, "Inited: c: " + currentAngle + " p: " + previousAngle + " a: " + angle);
        isInited = true;
    }

    @Override
    protected void finishSpin(double startX, double startY, double offsetX, double offsetY) {
        if (!isInited) {
            return;
        }
        final double angleDiff = normalizedAngle(currentAngle - previousAngle) / Math.abs(lastTime - previousLastTime);
        valueAnimator = new ValueAnimator();
        final float startAnimValue = (float) angleDiff;
        animationLastTime = System.nanoTime();
        valueAnimator.setFloatValues(startAnimValue, 0f);
        valueAnimator.setDuration(Math.min(Math.max((long) (200000000000.0 * Math.abs(angleDiff)), 200), 2000));
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                long time = animationLastTime;
                animationLastTime = System.nanoTime();
                applySpin(normalizedAngle(((Float) animation.getAnimatedValue() * Math.abs(animationLastTime - time)) + currentAngle));
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
                runAllocationAnimation();
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
        angle = previousAngle = currentAngle = 0;
        aX = aY = bX = bY = 0;
        a = b = c = 0;
        isAnimating = false;
        isInited = false;
        if (valueAnimator != null) {
            valueAnimator.cancel();
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

    private void runAllocationAnimation() {
        List<NineViewGroup.Box> placements = new ArrayList<>();
        List<View> unlocated = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            placements.add(NineViewGroup.Box.getByOrdinal(i));
            unlocated.add(getViewGroup().getSurroundingFrame(i));
        }
        NineViewGroup.Box placementBox = NineViewGroup.Box.getByOrdinal(0);
        View placementView = getViewGroup().getFrame(placementBox);
        double distance = Math.hypot(x0 * 2, y0 * 2);
        View movingView = null;
        for (View view : unlocated) {
            double currentDistance = getDistanceFromInitial(view, placementView);
            if (Double.compare(currentDistance, distance) < 0) {
                movingView = view;
                distance = currentDistance;
            }
        }
        movingView.setTranslationX(placementView.getLeft() - movingView.getLeft());
        movingView.setTranslationY(placementView.getTop() - movingView.getTop());

        for (int i = 1; i < 8; i++) {
            placementView = getViewGroup().getFrame(NineViewGroup.Box.getByOrdinal(i));
            movingView = getViewGroup().getFrame(getViewGroup().getBox(movingView).getNext());
            movingView.setTranslationX(placementView.getLeft() - movingView.getLeft());
            movingView.setTranslationY(placementView.getTop() - movingView.getTop());
        }
    }

    private double getDistanceFromInitial(View target, View initialView) {
        double tX = getInitialPositionX(target) + target.getTranslationX();
        double tY = getInitialPositionY(target) + target.getTranslationY();
        return Math.hypot(tX - getInitialPositionX(initialView), tY - getInitialPositionY(initialView));
    }
}
