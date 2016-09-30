package com.example.jonathan.openglexample;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


/**
 * Created by jonathan on 9/7/16.
 */
public class MyGLSurfaceView extends GLSurfaceView{

    private final MyGLRenderer mRenderer;
    private float mScaleFactor = 1.0f;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    private float mPreviousX;
    private float mPreviousY;
    private float mDensity;

    final DisplayMetrics displayMetrics = new DisplayMetrics();


    public MyGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,8,16,0);
        getHolder().setFormat(PixelFormat.RGBA_8888);

        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDensity = displayMetrics.density;

    }

    public boolean onTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();

        if (ev.getAction() == MotionEvent.ACTION_MOVE)
        {
            if (mRenderer != null)
            {
                float deltaX = (x - mPreviousX) / mDensity / 2f;
                float deltaY = (y - mPreviousY) / mDensity / 2f;

                mRenderer.addDeltaX(deltaX);
                mRenderer.addDeltaY(-deltaY);
            }
        }

        mPreviousX = x;
        mPreviousY = y;

        mScaleDetector.onTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);
        return true;
    }

    public void onResume(){
        mRenderer.start();
    }
    public void onPause(){
        mRenderer.stop();
        Log.d("MyGLSurfaceView", "Stopped renderer.");
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
            mRenderer.updateScaleMatrix(mScaleFactor);
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        public void onLongPress(MotionEvent e) {
            mRenderer.toggleLock();
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {

            if(e2.getPointerCount() != 2)
                return false;

            mRenderer.moveLight(distanceY/100.0f);
            return true;
        }
    }

}
