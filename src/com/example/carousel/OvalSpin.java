package com.example.carousel;

import android.view.View;

/**
 * Created by skamenkovych@codeminders.com on 7/9/2015.
 */
public class OvalSpin extends NineViewGroup.SpinStrategy {

    private float x0;
    private float y0;
    private double k;
    private double aX;
    private double aY;
    private double bX;
    private double bY;

    public OvalSpin(NineViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public double getAngle() {
        double startAngle = Math.atan2(aY, aX);
        double endAngle = Math.atan2(bY, bX);
        return endAngle - startAngle;
    }

    @Override
    protected float[] calculate(NineViewGroup.Box box, double distance) {
        float[] offset = new float[2];
        offset[0] = 0;
        offset[1] = 0;
        return offset;
    }

    @Override
    protected void spin(View target, double startX, double startY, double offsetX, double offsetY) {
        if (getViewGroup() == null || getViewGroup().getChildCount() != 9) {
            return;
        }
        if (offsetX == 0 && offsetY == 0) {
            k = 0;
            for (int i = 0; i < 7; i++) {
                View v = getViewGroup().getSurroundingFrame(0);
                v.setTranslationX(0);
                v.setTranslationY(0);
            }
            return;
        }
        View centerView = getViewGroup().getCenterFrame();
        x0 = centerView.getLeft() + centerView.getWidth() / 2;
        y0 = centerView.getTop() + centerView.getHeight() / 2;
        aX = startX - x0;
        aY = startY - y0;
        bX = startX + offsetX - x0;
        bY = startY + offsetY - y0;
    }
}
