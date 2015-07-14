package com.example.carousel;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by skamenkovych@codeminders.com on 7/8/2015.
 */
public class RectangleSpin extends NineViewGroup.SpinStrategy {

    public int left;
    public int top;
    public int right;
    public int bottom;
    private double aX;
    private double aY;
    private double bX;
    private double bY;
    private boolean isAnimating;
    private boolean isInited;
    private double currentAngle;
    private double previousAngle;
    private long lastTime;
    private ValueAnimator valueAnimator;

    public RectangleSpin(NineViewGroup viewGroup) {
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
    protected void spin(double startX, double startY, double offsetX, double offsetY) {
        if (getViewGroup() == null || getViewGroup().getChildCount() != 9) {
            return;
        }
        if (offsetX == 0 && offsetY == 0 || !isInited) {
            return;
        }
        View topLeftView = getViewGroup().getFrame(NineViewGroup.Box.TOP_LEFT);
        View bottomRightView = getViewGroup().getFrame(NineViewGroup.Box.BOTTOM_RIGHT);
        left = topLeftView.getLeft() + topLeftView.getWidth() / 2;
        top = topLeftView.getTop() + topLeftView.getHeight() / 2;
        right = bottomRightView.getLeft() + bottomRightView.getWidth() / 2;
        bottom = bottomRightView.getTop() + bottomRightView.getHeight() / 2;

        double distance = Math.abs(offsetX) + Math.abs(offsetY);
    }

    @Override
    protected void cancelSpin() {

    }

    @Override
    protected void initSpin(double startX, double startY, double offsetX, double offsetY) {
        isInited = false;

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

    private void runAllocationAnimation() {

    }

    @Override
    public void reset() {
        previousAngle = currentAngle = 0;
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

    @Override
    protected float[] calculateOffset(NineViewGroup.Box box, double distance) {
        double width = right - left;
        double height = bottom - top;
        float[] offset = new float[2];
        double temp;
        double signum = Math.signum(distance);
        distance = distance % getPerimeter();
        distance = (signum > 0) ? distance : getPerimeter() + distance;
        switch (box) {
            case TOP_LEFT:
                temp = height;
                if (distance < temp) {
                    offset[1] = (float) distance;
                    break;
                }
                offset[1] = (float) height;
                temp += width;
                if (distance < temp) {
                    offset[0] = (float) (distance - height);
                    break;
                }
                offset[0] = (float) width;
                temp += height;
                if (distance < temp) {
                    offset[1] = (float) (temp - distance);
                    break;
                }
                offset[1] = 0;
                offset[0] = (float) (width - distance + temp);
                break;
            default:
                break;
        }
        return offset;
    }

    private int getPerimeter() {
        return (right - left + bottom - top) << 1;
    }

    private void applySpin(double distance) {

    }
}
