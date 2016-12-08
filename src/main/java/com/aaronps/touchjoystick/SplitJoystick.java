package com.aaronps.touchjoystick;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * <p>Created by krom on 2016-05-06.</p>
 *
 * Simle usage
 * <pre>{@code
 *      final TouchJoystick joy = new TouchJoystick(listener, activity); // (this,this)
 * }</pre>
 *
 * Usage using custom metrics
 * <pre>{@code
 *     // step 1: create
 *     final TouchJoystick joy = new TouchJoystick(listener); // (this)
 *
 *     // step 2: configure
 *     final DisplayMetrics displayMetrics = new DisplayMetrics();
 *     getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
 *     joy.setMetrics(displayMetrics.widthPixels, displayMetrics.heightPixels);
 *
 *     // step 3: set as touch listener
 *     final View rootView = findViewById(android.R.id.content);
 *     rootView.setOnTouchListener(joy);
 * }
 * </pre>
 *
 */

public class SplitJoystick implements View.OnTouchListener {

    private static final String TAG = "SplitJoystick";
    // @todo Add a dead zone
    int mJoystickRadius = 0;

    int mLeftPressingPointerId = -1;
    int mLeftCenterX = -1;
    int mLeftCenterY = -1;
    int mLeftPos = -1;
    float mLeftForce = 0.0f;
    final Rect mLeftRect = new Rect();

    int mRightPressingPointerId = -1;
    int mRightCenterX = -1;
    int mRightCenterY = -1;
    int mRightPos = -1;
    float mRightForce = 0.0f;
    final Rect mRightRect = new Rect();

    final Listener mListener;

    public interface Listener {
        void onVerticalJoyChange(float value);
        void onHorizontalJoyChange(float value);
    }

    public SplitJoystick(final Listener listener) {
        mListener = listener;
    }

    public SplitJoystick(final Listener listener, final Activity activity)
    {
        mListener = listener;

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Log.d(TAG, displayMetrics.toString());

        setMetrics(displayMetrics.widthPixels, displayMetrics.heightPixels);

        activity.findViewById(android.R.id.content).setOnTouchListener(this);
    }

    public final void setMetrics(int width, int height) {
        // this radius is for a total diameter of 2/3 of the screen, seems too big.
//        final int baseRadius = ((Math.min(width, height) * 2) / 3) / 2;

        // this radius is for a total diameter of half of the screen.
        final int baseRadius = Math.min(width, height) / 4;

        final int middle = width / 2;

        // baseRadius / 2 allows the user more freedom on the initial press
        final int toInset = baseRadius / 2;

        mJoystickRadius = baseRadius;

        mLeftRect.set(0,0,middle,height);
        mRightRect.set(middle,0,width,height);

        mLeftRect.inset(toInset, toInset);
        mRightRect.inset(toInset, toInset);

        Log.d(TAG, "setMetrics: " + width + "x" + height + " = " + mLeftRect);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch ( event.getActionMasked() )
        {
            case MotionEvent.ACTION_DOWN: // the first touch
            case MotionEvent.ACTION_POINTER_DOWN: // the second touch or more
                int pointer_index = event.getActionIndex();
                onPointerPress( event.getPointerId(pointer_index),
                        Math.round(event.getX(pointer_index)),
                        Math.round(event.getY(pointer_index)));
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: //doc says not do anything on cancel
                onPointerRelease(event.getPointerId(event.getActionIndex()));
                break;

            case MotionEvent.ACTION_MOVE:
                for ( int i = 0, ie = event.getPointerCount(); i < ie; i++ )
                {
                    onPointerMove(  event.getPointerId(i),
                            Math.round(event.getX(i)),
                            Math.round(event.getY(i)));
                }
                break;


            default:
//                Log.d(TAG, "unknown: " + event);
                return false;

        }

        return true;
    }

    final void onPointerPress(final int pointerId, final int x, final int y) {
        if ( mLeftPressingPointerId == -1 && mLeftRect.contains(x, y) )
        {
            mLeftPressingPointerId = pointerId;
            mLeftCenterX = x;
            mLeftCenterY = y;
            mLeftPos = y;
//            Log.d(TAG, "onPointerPress: press LEFT " + pointerId);
        }
        else if ( mRightPressingPointerId == -1 && mRightRect.contains(x, y) )
        {
            mRightPressingPointerId = pointerId;
            mRightCenterX = x;
            mRightCenterY = y;
            mRightPos = x;
//            Log.d(TAG, "onPointerPress: RIGHT " + pointerId);
        }
    }

    final void onPointerRelease(final int pointerId) {
        if ( pointerId == mLeftPressingPointerId )
        {
            // release left
            mLeftPressingPointerId = -1;
            mLeftForce = 0.0f;
            mListener.onVerticalJoyChange(0.0f);
//            Log.d(TAG, "onPointerRelease: Left");
        }
        else if ( pointerId == mRightPressingPointerId )
        {
            // release right
            mRightPressingPointerId = -1;
            mRightForce = 0.0f;
            mListener.onHorizontalJoyChange(0.0f);
//            Log.d(TAG, "onPointerRelease: Right");
        }

    }

    final void onPointerMove(final int pointerId, final int x, final int y) {
        if ( pointerId == mLeftPressingPointerId )
        {
            // left move, only updown, ignore x
            if ( y != mLeftPos )
            {
                final int dif = mLeftCenterY - y;

                final float newForce = Math.min(1.0f, Math.max(-1.0f, (float)dif / mJoystickRadius));

                if ( newForce != mLeftForce )
                {
                    mLeftForce = newForce;
//                    Log.d(TAG, "onPointerMove: Left " + newForce);
                    mListener.onVerticalJoyChange(newForce);
                }

                mLeftPos = y;
            }
        }
        else if ( pointerId == mRightPressingPointerId )
        {
            // right move only leftright, ignore y
            if ( x != mRightPos )
            {
                final int dif = x - mRightCenterX;
                final float newForce = Math.min(1.0f, Math.max(-1.0f, (float)dif / mJoystickRadius));


                if ( newForce != mRightForce )
                {
                    mRightForce = newForce;
//                    Log.d(TAG, "onPointerMove: Right " + newForce);
                    mListener.onHorizontalJoyChange(newForce);
                }

                mRightPos = x;
            }
        }
    }

}
