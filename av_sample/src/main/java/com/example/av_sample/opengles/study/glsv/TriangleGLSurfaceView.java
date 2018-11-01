package com.example.av_sample.opengles.study.glsv;

import android.content.Context;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.example.av_sample.opengles.study.base.BaseGLSuifaceView;
import com.example.av_sample.opengles.study.shape.triangle.CameraTriangle;
import com.example.av_sample.opengles.study.shape.triangle.ColorfulTriangle;
import com.example.av_sample.opengles.study.shape.triangle.TriangleStudy;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *
 */
public class TriangleGLSurfaceView extends BaseGLSuifaceView {

    public TriangleGLSurfaceView(Context context) {
        super(context);
//        setRenderer(new TriangleRenderer());
//        setRenderer(new CameraTriangleRenderer());
        setRenderer(new ColorfulTriangleRenderer());
    }
    //1.设置版本 2.设置Render 3.指定图形处理

    public TriangleGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    //----------测试机：华为手机可行、oppo的不行
    private class TriangleRenderer implements Renderer{

        private TriangleStudy triangleStudy;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            triangleStudy = new TriangleStudy();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0,0,width,height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            triangleStudy.draw();
        }
    }

    //----------测试机：华为手机可行、oppo的不行
    private class CameraTriangleRenderer implements Renderer{

        private CameraTriangle triangleStudy;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            triangleStudy = new CameraTriangle();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            triangleStudy.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            triangleStudy.draw();
        }
    }

    class ColorfulTriangleRenderer implements Renderer {

        ColorfulTriangle triangle;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            triangle = new ColorfulTriangle();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            triangle.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            triangle.draw();
        }
    }
}
