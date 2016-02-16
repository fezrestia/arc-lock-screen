package com.fezrestia.android.application.arclockscreen.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class ArcLockScreenBaseView extends RelativeLayout {
//    private static final String TAG = "ArcLockScreenBaseView";

    protected final int DISPLAY_LONG_LINE_LENGTH;
    protected final int DISPLAY_SHORT_LINE_LENGTH;

    protected int mOrientation = Configuration.ORIENTATION_UNDEFINED;
    protected int mStatusBarHeight = 0;
    protected OnUnlockedListener mOnUnlockedListener;

    // CONSTRUCTOR.
    public ArcLockScreenBaseView(final Context context) {
        this(context, null);
        // NOP.
    }

    // CONSTRUCTOR.
    public ArcLockScreenBaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // NOP.
    }

    // CONSTRUCTOR.
    public ArcLockScreenBaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Get display size.
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        DISPLAY_LONG_LINE_LENGTH = Math.max(width, height);
        DISPLAY_SHORT_LINE_LENGTH = Math.min(width, height);

        // Get display orientation.
        if (height < width) {
            mOrientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            mOrientation = Configuration.ORIENTATION_PORTRAIT;
        }
    }

    @Override
    public void onSizeChanged(int curW, int curH, int lastW, int lastH) {
        if (curH < curW) {
            // Landscape.
            mStatusBarHeight = DISPLAY_SHORT_LINE_LENGTH - curH;
            mOrientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            // Portrait.
            mStatusBarHeight = DISPLAY_LONG_LINE_LENGTH - curH;
            mOrientation = Configuration.ORIENTATION_PORTRAIT;
        }
    }

    // Unlock event listener interface.
    public interface OnUnlockedListener {
        void onUnlock();
    }

    public void setOnUnlockedListener(OnUnlockedListener listener) {
        mOnUnlockedListener = listener;
    }
}
