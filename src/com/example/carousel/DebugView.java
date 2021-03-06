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
    private Paint pointPaint = new Paint();
    {
        ovalPaint.setColor(Color.GREEN);
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setTextSize(50);
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
        //ovalPaint.setStyle(Paint.Style.STROKE);
        //canvas.drawRect(ovalRect, ovalPaint);
        //canvas.drawOval(ovalRect, ovalPaint);
        ovalPaint.setStyle(Paint.Style.FILL);
        //for (int i = 0; i < 8; i++) {
        //    View v = nvGroup.getSurroundingFrame(i);
        //    canvas.drawCircle(v.getLeft() + v.getWidth()/2 + v.getTranslationX(),
        //            v.getTop() + v.getHeight()/2 + v.getTranslationY(),
        //            7, ovalPaint);
        //}
        if (nvGroup.getSpinStrategy() != null) {
            canvas.drawText("" + nvGroup.getSpinStrategy().calculateAngle(), 5, 50, pointPaint);
        }
    }

    public void setNineViewGroup(NineViewGroup group) {
        nvGroup = group;
    }

    private void setupOval() {
        if (nvGroup.getSpinStrategy() instanceof OvalSpin) {
            OvalSpin spin = (OvalSpin) nvGroup.getSpinStrategy();
            ovalRect.set(
                    (float) (spin.x0 - spin.b),
                    (float) (spin.y0 - spin.a),
                    (float) (spin.x0 + spin.b),
                    (float) (spin.y0 + spin.a)
            );
        }
        if (nvGroup.getSpinStrategy() instanceof RectangleSpin) {
            RectangleSpin spin = (RectangleSpin) nvGroup.getSpinStrategy();
            ovalRect.set(spin.left, spin.top, spin.right, spin.bottom);
        }
    }
}
