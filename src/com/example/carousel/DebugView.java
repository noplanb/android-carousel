package com.example.carousel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by skamenkovych@codeminders.com on 7/7/2015.
 */
public class DebugView extends View {
    private NineViewGroup nvGroup;
    private RectF ovalRect = new RectF();
    private Paint ovalPaint = new Paint();
    {
        ovalPaint.setColor(Color.GREEN);
    }
    public DebugView(Context context) {
        super(context);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DebugView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (nvGroup == null || nvGroup.getChildCount() != 9) {
            return;
        }
        setupOval();
        ovalPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(ovalRect, ovalPaint);
        ovalPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 8; i++) {
            View v = nvGroup.getSurroundingFrame(i);
            canvas.drawCircle(v.getLeft() + v.getWidth()/2 + v.getTranslationX(),
                    v.getTop() + v.getHeight()/2 + v.getTranslationY(),
                    7, ovalPaint);
        }
    }

    public void setNineViewGroup(NineViewGroup group) {
        nvGroup = group;
    }

    private void setupOval() {
        View topLeftV = nvGroup.getSurroundingFrame(7);
        View bottomRightV = nvGroup.getSurroundingFrame(2);
        ovalRect.set(
                topLeftV.getLeft() + topLeftV.getWidth() / 2,
                topLeftV.getTop() + topLeftV.getHeight() / 2,
                bottomRightV.getRight() - bottomRightV.getWidth() / 2,
                bottomRightV.getBottom() - bottomRightV.getHeight() / 2
        );
    }
}
