package com.example.jonathan.openglexample;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity{
    private MyGLSurfaceView mGLSurfaceView;
    private MyGLRenderer mRenderer;
    private CameraPreview cameraPreview;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Create and set cube on activity

        mGLSurfaceView = new MyGLSurfaceView(this);

        //mRenderer = new MyGLRenderer(this);
        //mGLSurfaceView.setRenderer(mRenderer);
        //mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(mGLSurfaceView);


        //  Create and set camera preview on activity
        cameraPreview = new CameraPreview( this );
        addContentView(cameraPreview , new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }


    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        //mRenderer.start();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        //mRenderer.stop();
        mGLSurfaceView.onPause();
    }




}
