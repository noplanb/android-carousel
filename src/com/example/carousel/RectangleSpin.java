package com.example.carousel;

import android.view.View;

/**
 * Created by skamenkovych@codeminders.com on 7/8/2015.
 */
public class RectangleSpin extends NineViewGroup.SpinStrategy {

    private int left;
    private int top;
    private int right;
    private int bottom;

    public RectangleSpin(NineViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public double getAngle() {
        return 0;
    }

    @Override
    protected void spin(View target, double startX, double startY, double offsetX, double offsetY) {
        if (getViewGroup() == null || getViewGroup().getChildCount() != 9) {
            return;
        }
        if (offsetX == 0 && offsetY == 0) {
            target.setTranslationY(0);
            target.setTranslationX(0);
            return;
        }
        View topLeftView = getViewGroup().getFrame(NineViewGroup.Box.TOP_LEFT);
        View bottomRightView = getViewGroup().getFrame(NineViewGroup.Box.BOTTOM_RIGHT);
        left = topLeftView.getLeft() + topLeftView.getWidth() / 2;
        top = topLeftView.getTop() + topLeftView.getHeight() / 2;
        right = bottomRightView.getLeft() + bottomRightView.getWidth() / 2;
        bottom = bottomRightView.getTop() + bottomRightView.getHeight() / 2;

        double distance = Math.abs(offsetX) + Math.abs(offsetY);
        NineViewGroup.Box targetBox = getViewGroup().getBox(target);
        float[] calculatedOffset = calculate(targetBox, distance);
        target.setTranslationX(calculatedOffset[0]);
        target.setTranslationY(calculatedOffset[1]);
    }

    private float getInitialPositionX(View v) {
        return v.getLeft() + v.getWidth() / 2;
    }

    private float getInitialPositionY(View v) {
        return v.getTop() + v.getHeight() / 2;
    }

    @Override
    protected float[] calculate(NineViewGroup.Box box, double distance) {
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
}
