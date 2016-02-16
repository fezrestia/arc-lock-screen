package com.fezrestia.android.application.arclockscreen.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.fezrestia.android.application.arclockscreen.ArcLockScreenApplication;
import com.fezrestia.android.application.arclockscreen.ExternalDependencyResolver;
import com.fezrestia.android.application.arclockscreen.R;
import com.fezrestia.android.application.arclockscreen.view.ArcLockScreenBaseView;

public class ArcLockScreenView extends ArcLockScreenBaseView {
    // Definitions.
    private final int ARROW_PIVOT_OFFSET = 40;
    private final int ARROW_ROT_LIMIT = 60;
    private final int RETURN_ANIMATION_DURATION = 500;
    private final int FAST_FADE_ANIMATION_DURATION = 300;
    private final int SLOW_FADE_ANIMATION_DURATION = 500;

    // Arrow degree.
    private int mArrowDeg = 0;
    private int mLastArrowDeg = 0;

    // Event listener.
    private TouchEventListenerOnArrow mArrowTouchListener = null;

    // View components.
    private View mArrowContainer = null; // Rotation target.
    private ImageView mBackArc = null; // Background.
    private ImageView mBackArcHighlighted = null;
    private ImageView mDestArrow = null;
    private View mArrowDestinationContainer = null;
    private View mGestureGuideGhostContainer = null;
    private ImageView mGestureGuideGhost = null;

    // Animations.
    private GhostAnimationListener mGhostAnimationListener = null;
    private AlphaAnimation mFadeInGhostAnimation = null;
    private RotateAnimation mRotateGhostAnimation = null;

    // Rapid boot setting.
    private boolean mIsRapidBootEnabled = false;

    // External application.
    private static final int CAMERA_KEY_LONG_PRESS_REPEATED_COUNT = 3;

    // CONSTRUCTOR.
    public ArcLockScreenView(final Context context) {
        this(context, null);
        // NOP.
    }

    // CONSTRUCTOR.
    public ArcLockScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // NOP.
    }

    // CONSTRUCTOR.
    public ArcLockScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Get rapid boot setting.
        SharedPreferences sp = getContext().getSharedPreferences(
                ArcLockScreenApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mIsRapidBootEnabled = sp.getBoolean(
                ArcLockScreenApplication.KEY_IS_RAPID_BOOT_ENABLED, false);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        // Get view components reference.
        mArrowContainer = findViewById(R.id.arrow_container);
        mBackArc = (ImageView) findViewById(R.id.back_arc);
        mBackArcHighlighted = (ImageView) findViewById(R.id.back_arc_highlighted);
        mArrowDestinationContainer = findViewById(R.id.destination_arrow_container);
        mDestArrow = (ImageView) findViewById(R.id.destination_arrow);
        mGestureGuideGhostContainer = findViewById(R.id.gesture_guide_ghost_container);
        mGestureGuideGhost = (ImageView) findViewById(R.id.gesture_guide_ghost);

        // Setup visibility.
        mArrowDestinationContainer.setVisibility(View.INVISIBLE);
        mDestArrow.setVisibility(View.INVISIBLE);
        mBackArcHighlighted.setVisibility(View.INVISIBLE);
        mGestureGuideGhostContainer.setVisibility(View.INVISIBLE);
        mGestureGuideGhost.setVisibility(View.INVISIBLE);

        // Setup touch event listener
        mArrowTouchListener = new TouchEventListenerOnArrow();
        View arrow = findViewById(R.id.arrow);
        arrow.setOnTouchListener(mArrowTouchListener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Setup and start animation here, because layout inflate finished here.
        // And width/height parameters of view can be get here.

        // Setup animations.
        mGhostAnimationListener = new GhostAnimationListener();

        mFadeInGhostAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFadeInGhostAnimation.setDuration(FAST_FADE_ANIMATION_DURATION * 3);
        mFadeInGhostAnimation.setFillAfter(true);
        mFadeInGhostAnimation.setAnimationListener(mGhostAnimationListener);
        mFadeInGhostAnimation.setInterpolator(new AccelerateInterpolator(1.0f));

        mRotateGhostAnimation = new RotateAnimation(-10.0f, 90.0f,
                mGestureGuideGhostContainer.getWidth(), mGestureGuideGhostContainer.getHeight());
        mRotateGhostAnimation.setDuration(RETURN_ANIMATION_DURATION * 3);
        mRotateGhostAnimation.setFillAfter(true);
        mRotateGhostAnimation.setRepeatCount(Animation.INFINITE);
        mRotateGhostAnimation.setRepeatMode(Animation.RESTART);
        mRotateGhostAnimation.setAnimationListener(mGhostAnimationListener);
        mRotateGhostAnimation.setInterpolator(new AccelerateInterpolator(1.5f));

        // Start animation.
        runGhost();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
         switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_CAMERA:
                // fall-through.
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    // Key is pressed.

                    if (keyEvent.getRepeatCount() == 0) {
                        // Broadcast "prepare" intent.
                        if (mIsRapidBootEnabled) {
                            getContext().sendBroadcast(
                                    new Intent(ExternalDependencyResolver
                                    .INTENT_ACTION_REQUEST_RAPID_BOOT_PREPARE)
                                    .setPackage(ExternalDependencyResolver
                                    .RAPID_BOOT_REQUEST_TARGET_PACKAGE));
                        }
                    } else if (keyEvent.getRepeatCount()
                            == CAMERA_KEY_LONG_PRESS_REPEATED_COUNT) {
                        // Broadcast "start" intent.
                        if (mIsRapidBootEnabled) {
                            getContext().sendBroadcast(
                                    new Intent(ExternalDependencyResolver
                                    .INTENT_ACTION_REQUEST_RAPID_BOOT_START));
                        }

                        // Vibrate.
                        Vibrator vibrator = (Vibrator) getContext()
                                .getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(30);

                        // Unlock.
                        if (mOnUnlockedListener != null) {
                            // Send unlock event.
                            mOnUnlockedListener.onUnlock();
                        }
                    }
                } else {
                    // Key is released.

                    // Broadcast "cancel" intent.
                    if (mIsRapidBootEnabled) {
                        getContext().sendBroadcast(
                                new Intent(ExternalDependencyResolver
                                .INTENT_ACTION_REQUEST_RAPID_BOOT_CANCEL)
                                .setPackage(ExternalDependencyResolver
                                .RAPID_BOOT_REQUEST_TARGET_PACKAGE));
                    }
                }
                break;

            default:
                // NOP.
                return super.dispatchKeyEvent(keyEvent);
        }

        return true;
    }

    public class TouchEventListenerOnArrow implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motion) {
            switch (motion.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Highlight arc.
                    highlightArc();

                    // Show destination arrow.
                    showDestinationArrow();
                    break;

                case MotionEvent.ACTION_MOVE:
                    mArrowDeg = getArrowDegFromMotionEvent(motion);

                    // Rotate arrow.
                    rotateArrow(mLastArrowDeg, mArrowDeg, 0); // Rapid rotate.

                    // Store current position.
                    updateLastArrowDeg(mArrowDeg);
                    break;

                case MotionEvent.ACTION_UP:
                    if (ARROW_ROT_LIMIT <= mArrowDeg) {
                        if (mOnUnlockedListener != null) {
                            // Send unlock event
                            mOnUnlockedListener.onUnlock();

                            // Broadcast "cancel" intent.
                            if (mIsRapidBootEnabled) {
                                getContext().sendBroadcast(
                                        new Intent(ExternalDependencyResolver
                                        .INTENT_ACTION_REQUEST_RAPID_BOOT_CANCEL)
                                        .setPackage(ExternalDependencyResolver
                                        .RAPID_BOOT_REQUEST_TARGET_PACKAGE));
                            }
                        }
                    } else {
                        // Reset arrow position.
                        cancelRotateArrowAndReturnDefaultPosition();
                    }

                    // Unhighlight arc.
                    unHighlightArc();

                    // Hide destination arrow.
                    hideDestinationArrow();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    // Force cancel.
                    cancelRotateArrowAndReturnDefaultPosition();

                    // Unhighlight arc.
                    unHighlightArc();

                    // Hide destination arrow.
                    hideDestinationArrow();
                    break;

                default:
                    // NOP.
                    break;
            }

            return true;
        }
    }

    private int getArrowDegFromMotionEvent(MotionEvent motion) {
        int x = 0;
        int y = 0;

        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            x = DISPLAY_LONG_LINE_LENGTH
                    - (int) motion.getRawX() - ARROW_PIVOT_OFFSET;
            y = DISPLAY_SHORT_LINE_LENGTH - mStatusBarHeight
                    - (int) motion.getRawY() - ARROW_PIVOT_OFFSET;
        } else {
            x = DISPLAY_SHORT_LINE_LENGTH
                    - (int) motion.getRawX() - ARROW_PIVOT_OFFSET;
            y = DISPLAY_LONG_LINE_LENGTH - mStatusBarHeight
                    - (int) motion.getRawY() - ARROW_PIVOT_OFFSET;
        }

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        return (int) (Math.atan((double) y / x) * 360.0 / Math.PI / 2.0);
    }

    // Return false if target degree is over limit.
    private void rotateArrow(int fromDeg, int toDeg, int duration) {
        // Calculate limit.
        int target = 0;
        if (toDeg < ARROW_ROT_LIMIT) {
            target = toDeg;
        } else {
            target = ARROW_ROT_LIMIT;
        }

        // Create rot animation.
        RotateAnimation rot = new RotateAnimation(fromDeg, target,
                mArrowContainer.getWidth(), mArrowContainer.getHeight());

        // Set variable parameters.
        rot.setDuration(duration);

        // Set fixed parameters.
        rot.setFillAfter(true);

        // Apply animation
        mArrowContainer.startAnimation(rot);
    }

    private void cancelRotateArrowAndReturnDefaultPosition() {
        rotateArrow(mArrowDeg, 0, RETURN_ANIMATION_DURATION); // 1sec animation.
        mLastArrowDeg = 0;
        mArrowDeg = 0;
    }

    private void updateLastArrowDeg(int currentDeg) {
        if (currentDeg < ARROW_ROT_LIMIT) {
            // ArrowDeg is not over limit.
            mLastArrowDeg = mArrowDeg;
        } else {
            // ArrowDeg is over limit, update last deg to limit value.
            mLastArrowDeg = ARROW_ROT_LIMIT;
        }
    }

    private void highlightArc() {
        mBackArc.clearAnimation();
        mBackArc.setVisibility(View.INVISIBLE);
        mBackArcHighlighted.clearAnimation();
        mBackArcHighlighted.setVisibility(View.VISIBLE);
    }

    private void unHighlightArc() {
        // Alpha definition.
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(FAST_FADE_ANIMATION_DURATION);
        fadeOut.setFillAfter(true);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(FAST_FADE_ANIMATION_DURATION);
        fadeIn.setFillAfter(true);

        // Start animation
        mBackArc.startAnimation(fadeIn);
        mBackArcHighlighted.startAnimation(fadeOut);
    }

    private void showDestinationArrow() {
        // Rotate definition.
        RotateAnimation rot = new RotateAnimation(ARROW_ROT_LIMIT, ARROW_ROT_LIMIT,
                mArrowDestinationContainer.getWidth(), mArrowDestinationContainer.getHeight());
        rot.setDuration(0); // No animation.
        rot.setFillAfter(true);

        // Clear animation.
        mArrowDestinationContainer.clearAnimation();
        mDestArrow.clearAnimation();

        // Apply animation.
        mArrowDestinationContainer.startAnimation(rot);

        // Show destination arrow.
        mArrowDestinationContainer.setVisibility(View.VISIBLE);
        mDestArrow.setVisibility(View.VISIBLE);
    }

    private void hideDestinationArrow() {
        // Alpha definition.
        AlphaAnimation fadeout = new AlphaAnimation(1.0f, 0.0f);
        fadeout.setDuration(SLOW_FADE_ANIMATION_DURATION);
        fadeout.setFillAfter(true);

        // Clear animation.
        mDestArrow.clearAnimation();

        // Fade out.
        mDestArrow.startAnimation(fadeout);
    }

    private void runGhost() {
        // Start animation.
        mGestureGuideGhostContainer.startAnimation(mRotateGhostAnimation);
    }

    private class GhostAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationEnd(Animation animation) {
            // NOP.
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            if (animation == mRotateGhostAnimation) {
                mGestureGuideGhost.startAnimation(mFadeInGhostAnimation);
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // NOP.
        }
    }
}
