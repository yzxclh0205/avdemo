package com.example.av_sample.opengles.study.shape.triangle;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 摄像机下的三角形
 */
public class CameraTriangle extends TriangleStudy{
//    attritude：一般用于各个顶点各不相同的量。如顶点颜色、坐标等。
//    uniform：一般用于对于3D物体中所有顶点都相同的量。比如光源位置，统一变换矩阵等。

    // 支持矩阵变换的顶点着色器
    public static final String vertexMatrixShaderCode =
            "attribute vec4 vPosition;"+
                    "uniform mat4 vMatrix;"+
                    "void main(){"+
                    "   gl_Position = vMatrix * vPosition;"+
                    "}";
    // 支持矩阵变换的顶点着色器
    public static final String vertexMatrixShaderCode2 =
            "attribute vec4 vPosition;\n" +
                    "uniform mat4 vMatrix;\n" +
                    "void main() {\n" +
                    "    gl_Position = vMatrix * vPosition;\n" +
                    "}";
    //投影矩阵
    private float[] mProjectionMatrix = new float[16];
    //视图矩阵
    private float[] mViewMatrix = new float[16];
    //模型视图投影矩阵
    private float[] mMVPMatrix = new float[16];

    private final FloatBuffer vertexBuffer;
    private final int mProgram;

    public CameraTriangle() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        mProgram = createOpenGLProgram(vertexMatrixShaderCode,fragmentShaderCode);
    }


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

    protected void handleMatrix(){
        //获取变换矩阵vMatrix成员句柄
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler,1,false,mMVPMatrix,0);
    }
}


