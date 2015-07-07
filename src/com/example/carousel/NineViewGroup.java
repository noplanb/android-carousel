/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.carousel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class NineViewGroup extends ViewGroup {
    // ---------
    // Constants
    // ---------
    private static final String TAG = NineViewGroup.class.getSimpleName();

    private static final float ASPECT = 240F / 320F;

    //-----------------------------
    // Abstract callback interfaces
    //-----------------------------
    private GestureCallbacks gestureCallbacks;
    private ViewGroupGestureRecognizer vgGestureRecognizer;
    private LayoutCompleteListener layoutCompleteListener;

    public interface GestureCallbacks {
        boolean onSurroundingClick(View view, int position);

        boolean onSurroundingStartLongpress(View view, int position);

        boolean onCenterClick(View view);

        boolean onCenterStartLongpress(View view);

        boolean onEndLongpress();

        boolean onCancelLongpress(int reason);
    }

    public interface LayoutCompleteListener {
        void onLayoutComplete();
    }

    //-------------
    // Constructors
    //-------------
    public NineViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public NineViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineViewGroup(Context context) {
        this(context, null);
    }

    private void init() {
        addElementViews();
        if (isInEditMode())
            return;
        addGestureRecognizer();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutElementViews();
        if (changed) {
            if (layoutCompleteListener != null)
                layoutCompleteListener.onLayoutComplete();
        }
    }

    //----------------------
    // Callback registration
    //----------------------
    public GestureCallbacks getGestureListener() {
        return gestureCallbacks;
    }

    public void setGestureListener(GestureCallbacks gl) {
        this.gestureCallbacks = gl;
    }

    public void setChildLayoutCompleteListener(LayoutCompleteListener childLayoutCompleteListener) {
        this.layoutCompleteListener = childLayoutCompleteListener;
    }

    //-------
    // Layout
    //-------
    private void addElementViews() {
        for (int i = 0; i < 9; i++) {
            FrameLayout fl = new FrameLayout(getContext());
            fl.setClipChildren(false);
            fl.setClipToPadding(false);
            fl.setId(i);
            setupTestView(fl);// FIXME remove
            addView(fl, i, new LayoutParams(elementWidth(), elementHeight()));
        }
    }

    private void layoutElementViews() {
        int x;
        int y;
        int i = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                x = (int) (gutterLeft() + col * (elementWidth() + paddingPx()));
                y = (int) (gutterTop() + row * (elementHeight() + paddingPx()));
                FrameLayout fl = (FrameLayout) getChildAt(i);
                fl.measure(MeasureSpec.EXACTLY | elementWidth(), MeasureSpec.EXACTLY | elementHeight());
                fl.layout(x, y, x + elementWidth(), y + elementHeight());
                i++;
            }
        }
    }

    // Layout helpers
    private Pair<Float, Float> elementSize() {
        float width;
        float height;
        if (isHeightConstrained()) {
            height = (getHeight() - 2 * (marginPx() + paddingPx())) / 3;
            width = ASPECT * height;
        } else {
            width = (getWidth() - 2 * (marginPx() + paddingPx())) / 3;
            height = width / ASPECT;
        }
        return new Pair<Float, Float>(width, height);
    }

    private int elementWidth() {
        return Math.round(elementSize().first);
    }

    private int elementHeight() {
        return Math.round(elementSize().second);
    }

    private boolean isHeightConstrained() {
        return !isWidthConstrained();
    }

    private boolean isWidthConstrained() {
        return (float) getWidth() / (float) getHeight() < ASPECT;
    }

    private Integer gutterTop() {
        if (isHeightConstrained())
            return (int) marginPx();
        else
            return (int) ((getHeight() - 3 * elementHeight() - 2 * paddingPx()) / 2);

    }

    private Integer gutterLeft() {
        if (isWidthConstrained())
            return (int) marginPx();
        else
            return (int) ((getWidth() - 3 * elementWidth() - 2 * paddingPx()) / 2);
    }

    private float paddingPx() {
        return getContext().getResources().getDimension(R.dimen.nine_view_padding);
    }

    private float marginPx() {
        return getContext().getResources().getDimension(R.dimen.nine_view_marging);
    }

    //---------------
    // Public getters
    //---------------
    public FrameLayout getCenterFrame() {
        return (FrameLayout) getChildAt(4);
    }

    /**
     * Returns view by desired populating order:
     * <pre>
     * 7 6 4
     * 5 8 0
     * 3 1 2
     * </pre>
     * <p/>
     * By converting from this internal index order
     * <pre>
     * 0 1 2
     * 3 4 5
     * 6 7 8
     * </pre>
     *
     * @return corresponding view
     */
    public FrameLayout getSurroundingFrame(int position) {
        return (FrameLayout) getChildAt(indexWithPosition(position));
    }

    private int indexWithPosition(int position) {
        switch (position) {
            case 0: return 5;
            case 1: return 7;
            case 2: return 8;
            case 3: return 6;
            case 4: return 2;
            case 5: return 3;
            case 6: return 1;
            case 7: return 0;
            case 8: return 4;
            default:
                throw new RuntimeException("Illegal position passed to getSurroundedView");
        }
    }

    private int positionWithIndex(int index) {
        switch (index) {
            case 0: return 7;
            case 1: return 6;
            case 2: return 4;
            case 3: return 5;
            case 4: return 8;
            case 5: return 0;
            case 6: return 3;
            case 7: return 1;
            case 8: return 2;
            default:
                throw new RuntimeException("Illegal position passed to getSurroundedView");
        }
    }

    //---------------------------
    // Private child view helpers
    //---------------------------
    private ArrayList<View> getNineViews() {
        ArrayList<View> r = new ArrayList<View>();
        for (int i = 0; i < getChildCount(); i++) {
            r.add(getChildAt(i));
        }
        return r;
    }

    @SuppressWarnings("ResourceType")
    private boolean isCenterView(View v) {
        return v.getId() == 4;
    }

    private int positionOfView(View v) {
        if (isCenterView(v))
            return -1;

        int pov = positionWithIndex(v.getId());
        if (pov > 7)
            throw new RuntimeException(TAG + " positionWithIndex: got out of bounds possition for view: " + pov);

        return pov;
    }

    //-------------------------------
    // NineViewGroupGestureRecognizer
    //-------------------------------
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return vgGestureRecognizer.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        vgGestureRecognizer.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return vgGestureRecognizer.onTouchEvent(event);
    }

    private void addGestureRecognizer() {
        vgGestureRecognizer = new NineViewGroupGestureRecognizer((Activity) getContext(), this, getNineViews());
        vgGestureRecognizer.enable();
    }

    private class NineViewGroupGestureRecognizer extends ViewGroupGestureRecognizer {
        public NineViewGroupGestureRecognizer(Activity a, ViewGroup vg, ArrayList<View> tvs) {
            super(a, vg, tvs);
        }

        @Override
        public boolean click(View v) {
            if (gestureCallbacks == null)
                return false;

            if (isCenterView(v))
                return gestureCallbacks.onCenterClick(v);
            return gestureCallbacks.onSurroundingClick(v, positionOfView(v));
        }

        @Override
        public boolean startLongpress(View v) {
            if (gestureCallbacks == null)
                return false;

            if (isCenterView(v))
                return gestureCallbacks.onCenterStartLongpress(v);
            return gestureCallbacks.onSurroundingStartLongpress(v, positionOfView(v));
        }

        @Override
        public boolean endLongpress(View v) {
            if (gestureCallbacks == null)
                return false;
            return gestureCallbacks.onEndLongpress();
        }

        @Override
        public boolean bigMove(View v) {
            return handleAbort(v, R.string.toast_dragged_finger_away);
        }

        @Override
        public boolean abort(View v, int reason) {
            return handleAbort(v, reason);
        }
    }

    protected boolean handleAbort(View v, int reason) {
        if (gestureCallbacks == null)
            return false;
        return gestureCallbacks.onCancelLongpress(reason);
    }

    @SuppressWarnings("ResourceType")
    private void setupTestView(FrameLayout fl) {
        if (isCenterView(fl)) {
            fl.setBackgroundColor(Color.parseColor("#9E000D"));
        } else {
            fl.setBackgroundColor(Color.parseColor("#5276C4"));
        }
        TextView tv = new TextView(getContext());
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        tv.setLayoutParams(p);
        tv.setTextColor(Color.YELLOW);
        tv.setTextSize(26f);
        tv.setText(String.valueOf(fl.getId()));
        fl.addView(tv);
    }
}
