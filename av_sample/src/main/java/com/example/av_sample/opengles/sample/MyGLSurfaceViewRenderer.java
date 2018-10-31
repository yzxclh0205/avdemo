package com.example.av_sample.opengles.sample;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by lh on 2018/10/29.
 */

public class MyGLSurfaceViewRenderer implements GLSurfaceView.Renderer{
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //设置背景，这里是黑色背景
        GLES20.glClearColor(1.0f,0.0f,0.0f,1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //系统环境变化，重新渲染
       GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}
