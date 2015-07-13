package com.example.carousel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MyActivity extends Activity {

    NineViewGroup nvGroup;
    DebugView debugView;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        nvGroup = (NineViewGroup) findViewById(R.id.group);
        debugView = (DebugView) findViewById(R.id.debug_view);
        debugView.setNineViewGroup(nvGroup);
        nvGroup.setGestureListener(new NineViewGroup.GestureCallbacks() {
            @Override
            public boolean onSurroundingClick(View view, int position) {
                showToast("onSurroundingClick " + position);
                return true;
            }

            @Override
            public boolean onSurroundingStartLongpress(View view, int position) {
                showToast("onSurroundingStartLongpress");
                return true;
            }

            @Override
            public boolean onEndLongpress() {
                showToast("onEndLongpress");
                return false;
            }

            @Override
            public boolean onCancelLongpress(int reason) {
                showToast("onCancelLongpress");
                return false;
            }

            @Override
            public void notifyUpdateDebug() {
                debugView.postInvalidate();
            }

            @Override
            public boolean onCenterClick(View view) {
                showToast("onCenterClick");
                nvGroup.getSpinStrategy().reset();
                notifyUpdateDebug();
                return true;
            }

            @Override
            public boolean onCenterStartLongpress(View view) {
                showToast("onCenterStartLongpress");
                return true;
            }
        });
        nvGroup.setSpinStrategy(new OvalSpin(nvGroup));
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
