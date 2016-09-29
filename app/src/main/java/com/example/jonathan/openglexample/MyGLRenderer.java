package com.example.jonathan.openglexample;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jonathan on 9/7/16.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer, SensorEventListener{

    private float mDeltaX = 0.0f;
    private float mDeltaY = 0.0f;
    private boolean locked;
    /** Store the accumulated rotation. */
    private final float[] mAccumulatedRotation = new float[16];

    /** Store the current rotation. */
    private final float[] mCurrentRotation = new float[16];


    private Cube mCube;
    private SensorManager mSensorManager;
    private Sensor mRVSensor;
    private Context context;


    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] mLightModelMatrix = new float[16];

    private float [] mScaleMatrix = new float[16];

    //  Used to hold the current position of the light in world space (after transformation via model matrix)
    private final float[] mLightPosInWorldSpace = new float[4];
    //  Used to hold the transformed position of the light in eye space (after transformation via modelview matrix)
    private final float[] mLightPosInEyeSpace = new float[4];
    //  Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
    //  we multiply this by our transformation matrices.
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    // Position the eye in front of the origin.
    final float eyeX = 0.0f;
    final float eyeY = 0.0f;
    final float eyeZ = -3.0f;

    // We are looking toward the distance
    final float lookX = 0.0f;
    final float lookY = 0.0f;
    final float lookZ = 0.0f;

    // Set our up vector. This is where our head would be pointing were we holding the camera.
    final float upX = 0.0f;
    final float upY = 1.0f;
    final float upZ = 0.0f;

    public MyGLRenderer(Context c){
        context = c;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mRVSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        locked = false;
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glDisable(GLES20.GL_DITHER);
        // Set the background frame color
        GLES20.glClearColor(0,0,0,0);
        // Enable depth test
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        //GLES20.glDepthFunc(GLES20.GL_EQUAL);
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);


        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix,0);
        Matrix.setIdentityM(mRotationMatrix,0);
        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(mAccumulatedRotation, 0);
        mCube = new Cube(context);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 2.0f;
        final float far = 6.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    float[] scratch = new float[16];
    float[] scratchScale = new float[16];
    float[] scratchRotation = new float[16];
    @Override
    public void onDrawFrame(GL10 gl) {
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        // Redraw background color

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        // Enable depth test

        Matrix.setIdentityM(mLightModelMatrix, 0);

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);




        if(!locked){
            Matrix.setIdentityM(mCurrentRotation, 0);
            Matrix.rotateM(mCurrentRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(mCurrentRotation, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
            mDeltaX = 0.0f;
            mDeltaY = 0.0f;

            // Multiply the current rotation by the accumulated rotation, and then set the accumulated
            // rotation to the result.
            //mAccumulatedRotation
            Matrix.multiplyMM(scratchRotation, 0, mCurrentRotation, 0, mRotationMatrix, 0);
            System.arraycopy(scratchRotation, 0, mRotationMatrix, 0, 16);
        }


        // Set the camera position
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        //NEW CODE
        Matrix.multiplyMM(scratchScale, 0, mMVPMatrix, 0, mScaleMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.

        //NEW CODE
        Matrix.multiplyMM(scratch, 0, scratchScale, 0, mRotationMatrix, 0);
        //Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw shape
        mCube.draw(scratch, mLightPosInEyeSpace);

        //  Draw light source
        float[]scratchLight = new float[16];
        // Pass in the transformation matrix.
        Matrix.multiplyMM(scratchLight, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(scratchLight, 0, mProjectionMatrix, 0, scratchLight, 0);

        mCube.drawLight(scratchLight, mLightPosInEyeSpace);


    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void start(){
        mSensorManager.registerListener(this, mRVSensor, 10000);//SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        // make sure to turn our sensor off when the activity is paused
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(!locked)
            return;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix , sensorEvent.values);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){}

    public void updateScaleMatrix(float scale){
        Matrix.setIdentityM(mScaleMatrix,0);
        Matrix.scaleM(mScaleMatrix, 0, scale, scale, scale);
    }


    public void addDeltaX(float deltaX){
        mDeltaX += deltaX;
    }
    public void addDeltaY(float deltaY){
        mDeltaY += deltaY;
    }
    public void toggleLock(){
        locked = (locked)?false:true;
    }
}
