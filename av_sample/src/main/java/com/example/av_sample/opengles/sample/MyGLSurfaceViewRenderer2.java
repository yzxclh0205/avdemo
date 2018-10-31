package com.example.av_sample.opengles.sample;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by lh on 2018/10/29.
 */

public class MyGLSurfaceViewRenderer2 implements GLSurfaceView.Renderer{
    // initialize a triangle
    private Triangle mTriangle;
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //设置背景，这里是黑色背景
        mTriangle = new Triangle();
//        GLES20.glClearColor(1.0f,0.0f,0.0f,1.0f);
    }

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];


    //定义投影视图
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //系统环境变化，重新渲染
       GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / height;
        //这些参数是啥意思
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    //定义相机视图
    @Override
    public void onDrawFrame(GL10 gl) {
        float[] scratch = new float[16];
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        long time = SystemClock.uptimeMillis() & 4000L;
        float angle = 0.09f * ((int)time);
        Matrix.setRotateM(mRotationMatrix,0,angle,0,0,-1);
        Matrix.multiplyMM(scratch,0,mMVPMatrix,0,mRotationMatrix,0);
        mTriangle.draw(scratch);
    }
}
