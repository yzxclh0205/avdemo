package com.example.av_sample.opengles.study.glsv;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.av_sample.opengles.study.base.BaseGLSurfaceView;
import com.example.av_sample.opengles.study.shape.oval.Cone;
import com.example.av_sample.opengles.study.shape.oval.Cylinder;
import com.example.av_sample.opengles.study.shape.oval.Oval;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 绘制圆形的GLSurfaceView
 */
public class OvalGLSurfaceView extends BaseGLSurfaceView {

    public OvalGLSurfaceView(Context context) {
        super(context);

//        setRenderer(new OvaleRenderer());
//        setRenderer(new ConeRenderer());
        setRenderer(new CylinderRenderer());

//        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    class OvaleRenderer implements GLSurfaceView.Renderer {

        Oval oval;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            oval = new Oval();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            oval.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            oval.draw();
        }
    }


    class ConeRenderer implements GLSurfaceView.Renderer {

        Cone cone;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            cone = new Cone();
            cone.onSurfaceCreate();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            cone.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            cone.draw();
        }
    }


    /**
     * 圆柱体渲染器
     */
    class CylinderRenderer implements GLSurfaceView.Renderer {

        Cylinder cylinder;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            cylinder = new Cylinder();
            cylinder.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            cylinder.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            cylinder.draw();
        }
    }
//
//    /**
//     * 球体渲染器
//     */
//    class BallRenderer implements GLSurfaceView.Renderer {
//
//        Ball ball = new Ball();
//
//        @Override
//        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//            ball.onSurfaceCreated();
//        }
//
//        @Override
//        public void onSurfaceChanged(GL10 gl, int width, int height) {
//            ball.onSurfaceChanged(width, height);
//        }
//
//        @Override
//        public void onDrawFrame(GL10 gl) {
//            ball.draw();
//        }
//    }
//
//    /**
//     * 带光源球体渲染器
//     */
//    class BallWithLightRenderer implements GLSurfaceView.Renderer {
//
//        BallWithLight ballWithLight = new BallWithLight();
//
//        @Override
//        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//            ballWithLight.onSurfaceCreated();
//        }
//
//        @Override
//        public void onSurfaceChanged(GL10 gl, int width, int height) {
//            ballWithLight.onSurfaceChanged(width, height);
//        }
//
//        @Override
//        public void onDrawFrame(GL10 gl) {
//            ballWithLight.draw();
//        }
//    }

}