package com.example.av_sample.opengles.study.shape.square;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.av_sample.opengles.study.base.BaseGLSL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL;

/**
 * 1.顶点，片元着色器代码
 * 2.顶点坐标
 * 3.颜色数组
 * 4.创建 程序
 * 5.
 */

public class Square extends BaseGLSL{

    private final String vertexShaderCode =
            "attribute vec4 vPosition;"+
                    "uniform mat4 vMatrix;"+
                    "void main(){"+
                    " gl_Position = vMatrix * vPosition;"+
                    "}"
            ;
    private final String fragSharderCode =
            "precision mediump float;"+
                    "uniform vec4 vColor;"+
                    "void main(){"+
                    "gl_FragColor = vColor;"+
            "}";

    static float triangleCoords[] = {
            -0.5f, 0.5f, 0.0f, // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f, 0.5f, 0.0f  // top right
    };
    static short index[] = {0,1,2,0,3,2};
    //设置颜色，依次为红绿蓝和透明通道
    float color[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;

    public Square() {
        initBytebuffer();

    }

    private void initBytebuffer() {
        //注意这里乘以的4
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(index.length * 2);
        byteBuffer1.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuffer1.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

        mProgram = createOpenGLProgram(vertexShaderCode, fragSharderCode);

    }

    public void onSurfaceChanged(int width, int height) {
        float ratio = (float) width / height;
        //设置投影位置
        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,0,0,7.0f,0,0f,0f,0f,1f,0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    public void draw() {
        GLES20.glUseProgram(mProgram);
        int vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(vMatrix,1,false,mMVPMatrix,0);

        int vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition,COORDS_PER_VERTEX,GLES20.GL_FLOAT,false,vertexStride,vertexBuffer);

        int vColor = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(vColor,1,color,0);

        //索引法绘制正方形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,index.length,GLES20.GL_UNSIGNED_SHORT,indexBuffer);

        GLES20.glDisableVertexAttribArray(vPosition);

    }
}
