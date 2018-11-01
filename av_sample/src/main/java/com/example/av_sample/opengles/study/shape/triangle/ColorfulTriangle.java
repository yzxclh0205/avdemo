package com.example.av_sample.opengles.study.shape.triangle;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 摄像机下的三角形
 */
public class ColorfulTriangle extends TriangleStudy{
//    attritude：一般用于各个顶点各不相同的量。如顶点颜色、坐标等。
//    uniform：一般用于对于3D物体中所有顶点都相同的量。比如光源位置，统一变换矩阵等。

    // 支持矩阵变换的顶点着色器
    public static final String vertexMatrixShaderCode =
            "attribute vec4 vPosition;"+
                    "uniform mat4 vMatrix;"+
                    "varying vec4 vColor;"+
                    "attribute vec4 aColor;"+
                    "void main(){"+
                    "   gl_Position = vMatrix * vPosition;"+
                    "   vColor = aColor;"+
                    "}";

    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    //视图矩阵
    private float[] mViewMatrix = new float[16];
    //模型视图投影矩阵
    private float[] mMVPMatrix = new float[16];

    private final FloatBuffer colorBuffer;


    public ColorfulTriangle() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        mProgram = createOpenGLProgram(vertexMatrixShaderCode,fragmentColorShaderCode);

    }

    private final String fragmentColorShaderCode =
            "precision mediump float;\n" +
                    "varying vec4 vColor;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = vColor;\n" +
                    "}";
    // 简单的片元着色器---------------------------------------------- 注意，这里的vColor的修饰符，varying表示易变量，一般用于顶点着色器传递到片元着色器的量。
    public static final String fragmentShaderCode =
            " precision mediump float;\n" +
                    " uniform vec4 vColor;\n" +
                    " void main() {\n" +
                    "     gl_FragColor = vColor;\n" +
                    " }";
    //设置颜色
    float color[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };


    public void onSurfaceChanged(int width, int height) {
        //计算宽高比
        float ratio = (float)width / height;
        //设置透视投影
        Matrix.frustumM(mProjectionMatrix,0,-ratio,ratio,-1,1,3,7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,0,0,7f,0f,0f,0f,0f,1f,0);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mViewMatrix,0);
    }

    public void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        super.draw();
    }
    public void draw1() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        //获取变换矩阵vMatrix成员句柄
//        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
//        //指定vMatrix的值
//        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        handleMatrix();
//        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        handleColor();
//        //获取片元着色器的vColor成员的句柄
//        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
//        //设置绘制三角形的颜色
//        GLES20.glEnableVertexAttribArray(mColorHandle);
//        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    protected void handleMatrix(){
        //获取变换矩阵vMatrix成员句柄
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler,1,false,mMVPMatrix,0);
    }

    public void handleColor(){
//        //获取片元着色器的vColor成员的句柄
//        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        int mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle,4,GLES20.GL_FLOAT,false,0,colorBuffer);


    }
}


