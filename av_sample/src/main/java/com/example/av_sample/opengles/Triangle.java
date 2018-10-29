package com.example.av_sample.opengles;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by lh on 2018/10/29.
 */

public class Triangle {

    //------------------------------- 定义形状  -------------------------------
    // 三角形 上，左，右三个点
    private static float triangleCoords[] = {
            0.0f, 0.622008459f, 0.0f,
            -0.5f, -0.311004243f, 0.0f,
            0.5f, -0.311004243f, 0.0f
    };
    // 数组中每个顶点几个坐标；number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    // 顶点个数
    public static final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    //每个坐标4个字节 4 bytes per vertex
    private static  int vertexStride = COORDS_PER_VERTEX * 4;

    private float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};
    private FloatBuffer vertexBuffer;
    private int mProgram;
    private int mPositionHandle;

    public Triangle() {
        //坐标点个数 * 4 （每一 四个字节）
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        //使用硬件排序
        byteBuffer.order(ByteOrder.nativeOrder());
        //从ByteBuffer中创建一个floatByteBuffer
        vertexBuffer = byteBuffer.asFloatBuffer();
        //添加坐标点到FloatBuffer
        vertexBuffer.put(triangleCoords);
        //设置 图形的第一个点
        vertexBuffer.position(0);

        initDraw();
    }


    //----------------------------- 绘制形状 -----------------------------
    //定义形状着色器和颜色纹理做色漆的 图形代码
    // 简单的顶点着色器
    public static final String vertexShaderCode =
            "attribute vec4 vPosition;\n" +
                    " void main() {\n" +
                    "     gl_Position   = vPosition;\n" +
                    " }";

    // 简单的片元着色器
    public static final String fragmentShaderCode =
            " precision mediump float;\n" +
                    " uniform vec4 vColor;\n" +
                    " void main() {\n" +
                    "     gl_FragColor = vColor;\n" +
                    " }";
    //编译shader代码 ，将顶点着色器和颜色纹理着色器添加到程序中，并链接
    private void initDraw() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f); // 申请底色空间
        int vertexShader = ShaderUtil.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = ShaderUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram,vertexShader);
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("Triangle", "Could not link program:" + GLES20.glGetProgramInfoLog(mProgram));
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }

    //定义如何绘制，绘制什么
    public void draw() {
        //1.添加程序到OpenGL ES 环境
        GLES20.glUseProgram(mProgram);
        //2.从顶点着色器的vPostion属性中获取属性作为处理器（int）
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //3.设置允许处理器执行
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //4.准备图形的坐标数据------------------------------------------------------表示疑问
        GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT,false,vertexStride,vertexBuffer);
        //5.从颜色、纹理着色器vColor属性中获取Uniform 作为处理器（int）
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //6.设置处理器 颜色
        GLES20.glUniform4fv(mColorHandle,1,color,0);
        //7.绘制。设置绘制的点的开启，结束为止
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount);
        //8.停止绘制，禁止着色器绘制
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }

//    public void draw() {
//        //将程序加入到OpenGLES2.0环境
//        GLES20.glUseProgram(mProgram);
//        //获取顶点着色器的vPosition成员句柄
//        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
//        //启用三角形顶点的句柄
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
//        //准备三角形的坐标数据
//        GLES20.glVertexAttribPointer(mPositionHandle, Triangle.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, Triangle.vertexStride, vertexBuffer);
//        //获取片元着色器的vColor成员的句柄
//        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
//        //绘制三角形
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, Triangle.vertexCount);
//        //禁止顶点数组的句柄
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//    }
}
