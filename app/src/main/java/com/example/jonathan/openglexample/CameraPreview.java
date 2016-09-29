package com.example.jonathan.openglexample;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Jona Q on 9/19/2016.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;

    static private Camera mCamera = null;
    Camera.Parameters params;
    private int CAMERA_ORIENTATION = 90;

    public CameraPreview(Context context){
        super(context);

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera.setDisplayOrientation(CAMERA_ORIENTATION);
        //set camera to continually auto-focus
        params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        if(mCamera == null){
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCamera.setDisplayOrientation(CAMERA_ORIENTATION);
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("Error", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("orientation", "portrait");
            params.set("orientation", "portrait");
            params.set("rotation", 90);
            CAMERA_ORIENTATION = 90;
        }
        else {
            Log.d("orientation", "landscape");
            params.set("orientation", "landscape");
            params.set("rotation", 90);

            CAMERA_ORIENTATION = 90;
        }
        mCamera.setParameters(params);
        //mCamera.setDisplayOrientation(CAMERA_ORIENTATION);

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Error", "Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * Release the camera. Called when the parent Activity's onPause()
     * method is called
     * @param surfaceHolder
     */
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }



}
