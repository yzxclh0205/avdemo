package com.example.av_sample.opengles.study.shape.oval;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.av_sample.opengles.study.base.BaseGLSL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by lh on 2018/11/2.
 */

public class Cone extends BaseGLSL {
    private final static String vertexShaderCode =
            "uniform mat4 vMatrix;\n" +
                    "varying vec4 vColor;\n" +
                    "attribute vec4 vPosition;\n" +
                    "void main(){\n" +
                    "    gl_Position=vMatrix*vPosition;\n" +
                    "    if(vPosition.z==0.0){\n" +
                    "        vColor=vec4(0.5,0.5,0.5,1.0);\n" +
                    "    }else{\n" +
                    "        vColor=vec4(0.9,0.9,0.9,1.0);\n" +
                    "    }\n" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mProgram;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    //设置颜色，依次为红绿蓝和透明通道
    float color[] = {1.0f, 1.0f, 1.0f, 1.0f};
    float[] shapePos;

    float radius = 1.0f;
    int n = 360;
    private float height = 0.0f;
    private FloatBuffer vertexBuff;

    public Cone() {
        this(2.0f);
    }

    private Oval oval;
    public Cone(float v) {
        oval = new Oval();
        this.height = v;
        shapePos = createPositions();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(shapePos.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuff = byteBuffer.asFloatBuffer();
        vertexBuff.put(shapePos);
        vertexBuff.position(0);
        mProgram = createOpenGLProgram(vertexShaderCode,fragmentShaderCode);
    }

    private float[] createPositions() {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f);
        data.add(0.0f);
        data.add(height);
        float anDegSpan = 360/n;
        for(int i=0;i<=n;i+=anDegSpan){
            data.add((float) (radius * Math.sin(i * Math.PI /180)));
            data.add((float) (radius * Math.cos(i * Math.PI /180)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for(int i=0;i<f.length;i++){
            f[i] = data.get(i);
        }
        return f;

    }

    public void onSurfaceChanged(int width, int height) {
        //计算宽高比
        float ratio = (float) width / height;
//        //设置透视投影
//        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
//        //设置相机位置
//        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//        //计算变换矩阵
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 1.0f, -10.0f, -4.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    public void setMatrix(float[] matrix) {
        this.mMVPMatrix = matrix;
    }

    public void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        //获取变换矩阵vMatrix成员句柄
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        //获取顶点着色器的vPosition成员句柄
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuff);
        //获取片元着色器的vColor成员的句柄
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, shapePos.length / 3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        oval.setMatrix(mMVPMatrix);
        oval.draw();
    }

    public void onSurfaceCreate() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
