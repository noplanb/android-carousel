package com.example.carousel;

import android.view.View;

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

    public OvalSpin(NineViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public double getAngle() {
        double startAngle = Math.atan2(aY, aX);
        double endAngle = Math.atan2(bY, bX);
        double diff = endAngle - startAngle;
        if (diff > Math.PI) {
            diff -= 2*Math.PI;
        } else if (diff < -Math.PI) {
            diff += 2*Math.PI;
        }
        return diff;
    }

    @Override
    protected float[] calculate(NineViewGroup.Box box, double distance) {
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
        if (offsetX == 0 && offsetY == 0) {
            //k = 0;
            //for (int i = 0; i < 8; i++) {
            //    View v = getViewGroup().getSurroundingFrame(i);
            //    v.setTranslationX(0);
            //    v.setTranslationY(0);
            //}
            return;
        }
        aX = startX - x0;
        aY = startY - y0;
        bX = startX + offsetX - x0;
        bY = startY + offsetY - y0;
        double newAngleOffset = getAngle();
        for (int i = 0; i < 8; i++) {
            View v = getViewGroup().getSurroundingFrame(i);
            float[] offset = calculate(getViewGroup().getBox(v), newAngleOffset);
            v.setTranslationX(offset[0]);
            v.setTranslationY(offset[1]);
        }
    }

    @Override
    protected void cancelSpin() {

    }

    @Override
    protected void initSpin(double startX, double startY, double offsetX, double offsetY) {
        View centerView = getViewGroup().getCenterFrame();
        x0 = getInitialPositionX(centerView);
        y0 = getInitialPositionY(centerView);
        View topLeftView = getViewGroup().getFrame(NineViewGroup.Box.TOP_LEFT);
        double left = getInitialPositionX(topLeftView);
        double top = getInitialPositionY(topLeftView);
        c = y0 - top;
        b = x0 - left;
        a = Math.hypot(b, c);
        angle = angle + getAngle();
    }

    @Override
    public void reset() {
        x0 = y0 = 0;
        angle = 0;
        aX = aY = bX = bY = 0;
        a = b = c = 0;
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
}
