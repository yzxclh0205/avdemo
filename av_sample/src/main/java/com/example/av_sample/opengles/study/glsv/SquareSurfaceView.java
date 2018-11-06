package com.example.av_sample.opengles.study.glsv;

import android.content.Context;
import android.util.AttributeSet;

import com.example.av_sample.opengles.study.base.BaseGLSurfaceView;
import com.example.av_sample.opengles.study.shape.square.Cube;
import com.example.av_sample.opengles.study.shape.square.Square;
import com.example.av_sample.opengles.study.shape.square.VaryMatrixCube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by lh on 2018/10/31.
 */

public class SquareSurfaceView extends BaseGLSurfaceView {
    public SquareSurfaceView(Context context) {
        super(context);

//        setRenderer(new SquareRenderer()); // 绘制正方形
//        setRenderer(new CubeRenderer()); // 绘制立方体
        setRenderer(new VaryMatrixCubeRenderer()); // 多种矩阵变换的正方体
    }

    public SquareSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    private class SquareRenderer implements Renderer{

        private Square square;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            square = new Square();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            square.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            square.draw();
        }
    }

    private class  CubeRenderer implements Renderer{

        private Cube cube;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            cube = new Cube();
            cube.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            cube.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            cube.draw();
        }
    }

    class VaryMatrixCubeRenderer implements Renderer {

        VaryMatrixCube cube;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            cube = new VaryMatrixCube();
            cube.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            cube.onSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            cube.draw();
        }
    }
}
