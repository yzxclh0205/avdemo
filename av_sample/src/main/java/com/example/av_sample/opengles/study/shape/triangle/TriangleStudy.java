package com.example.av_sample.opengles.study.shape.triangle;

import android.opengl.GLES20;

import com.example.av_sample.opengles.study.base.BaseGLSL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 普通三角形：
 * 1.顶点着色器代码、片元着色器代码
 * 2.顶点坐标
 * 3.颜色数组
 * 4.顶点个数
 * 5.构造时 创建顶点坐标buff，program
 * 6.绘制 1.将程序加入到OpenGL2.0环境中 2.获取顶点着色器mPostion句柄 3.启用三角形顶点句柄
 * 4.获取片元着色器的vColor成员的句柄。5设置绘制三角形的颜色
 * 7.绘制三角形
 * 8.禁止顶点数组的句柄
 */
public class TriangleStudy extends BaseGLSL {


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
    //定义三角形的坐标
    public static float triangleCoords[] = {
            0.5f,0.5f,0.0f,
            -0.5f,-0.5f,0f,
            0.5f,-0.5f,0f
    };
    //定义三角形的颜色 -白色
//    public static float color[] = {1f,1f,1f,1f};
    //设置颜色
    float color[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };
    //顶点的个数
    public static final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    public  FloatBuffer vertexBuffer;
    public  int mProgram;


    public TriangleStudy() {
//        //申请底色空间
//        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);
//        //处理顶点数据ByteBuff
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
//        byteBuffer.order(ByteOrder.nativeOrder());
//        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
//        vertexBuffer = byteBuffer.asFloatBuffer();
//        vertexBuffer.put(triangleCoords);
//        vertexBuffer.position(0);
//
//        //创建程序
//        mProgram = createOpenGLProgram(vertexShaderCode, fragmentShaderCode);
    }
    protected int mPositionHandle;
    public void draw() {
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        handleMatrix();
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        handleColor();
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void handleColor() {
        //获取片元着色器的vColor成员的句柄
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
    }

    protected void handleMatrix() {

    }
    //void glVertexAttribPointer (int index, int size, int type, boolean normalized, int stride, Buffer ptr )
//    参数含义：
//    index 指定要修改的顶点着色器中顶点变量id；
//    size 指定每个顶点属性的组件数量。必须为1、2、3或者4。如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））；
//    type 指定数组中每个组件的数据类型。可用的符号常量有GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT,GL_UNSIGNED_SHORT, GL_FIXED, 和 GL_FLOAT，初始值为GL_FLOAT；
//    normalized 指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）；
//    stride 指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0。如果normalized被设置为GL_TRUE，意味着整数型的值会被映射至区间-1,1，或者区间[0,1]（无符号整数），反之，这些值会被直接转换为浮点值而不进行归一化处理；
//    ptr 顶点的缓冲数据。
//
}
