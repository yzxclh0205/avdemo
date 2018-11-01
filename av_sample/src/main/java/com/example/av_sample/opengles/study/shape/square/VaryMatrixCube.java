package com.example.av_sample.opengles.study.shape.square;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.av_sample.opengles.study.base.BaseGLSL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 多种矩阵变换的正方体
 */

public class VaryMatrixCube extends BaseGLSL{

    private final String vertexShaderCode =
            "attribute vec4 vPosition;"+
                    "uniform mat4 vMatrix;" +
                    "varying vec4 vColor;" +
                    "attribute vec4 aColor;"+
                    "void main(){"+
                    " gl_Position = vMatrix * vPosition;"+
                    " vColor = aColor;"+
                    "}"
            ;
    private final String fragSharderCode =
            "precision mediump float;"+
                    "varying vec4 vColor;"+  //-------------------------uniform 改成
                    "void main(){"+
                    "gl_FragColor = vColor;"+
            "}";

    final float cubePositions[] = {
            -1.0f, 1.0f, 1.0f,    //正面左上0
            -1.0f, -1.0f, 1.0f,   //正面左下1
            1.0f, -1.0f, 1.0f,    //正面右下2
            1.0f, 1.0f, 1.0f,     //正面右上3
            -1.0f, 1.0f, -1.0f,    //反面左上4
            -1.0f, -1.0f, -1.0f,   //反面左下5
            1.0f, -1.0f, -1.0f,    //反面右下6
            1.0f, 1.0f, -1.0f,     //反面右上7
    };
    final short index[] = {
            6, 7, 4, 6, 4, 5,    //后面
            6, 3, 7, 6, 2, 3,    //右面
            6, 5, 1, 6, 1, 2,    //下面
            0, 3, 2, 0, 2, 1,    //正面
            0, 1, 5, 0, 5, 4,    //左面
            0, 7, 3, 0, 4, 7,    //上面
    };

    float color[] = {
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
    };

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;
    private FloatBuffer colorBuffer;

    public VaryMatrixCube() {
        initBytebuffer();
        mProgram = createOpenGLProgram(vertexShaderCode,fragSharderCode);

    }

    private void initBytebuffer() {
        //注意这里乘以的4
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cubePositions.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(cubePositions);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(index.length * 2);
        byteBuffer1.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuffer1.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

        mProgram = createOpenGLProgram(vertexShaderCode, fragSharderCode);

        ByteBuffer dd = ByteBuffer.allocateDirect(color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

    }

    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / height;
//        //设置投影位置
//        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,20);
//        //设置相机位置
//        Matrix.setLookAtM(mViewMatrix,0,9f,5.0f,10.0f,0,0f,0f,0f,1f,0f);
//        //计算变换矩阵
//        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
//
        //设置投影位置
        ortho(-ratio*6,ratio*6,-6,6,3,20);
        //设置相机位置
        setCamera(0,0,10,0,0,0,0,1,0);
        //计算变换矩阵
//        translate(0,3,0);

        // y轴负方向平移，然后按xyz->(0,0,0)到(1,1,1)旋转30度
//         translate(0, -3, 0);
//         rotate(30f, 1, 1, 1);

        // x轴负方向平移，然后按xyz->(0,0,0)到(1,-1,1)旋转120度，在放大到0.5倍
//         translate(-3, 0, 0);
//         rotate(120f, 1, -1, 1);
//         scale(0.5f, 0.5f, 0.5f);

        translate(1, 0, 0);
        scale(1.0f, 2.0f, 1.0f);
        rotate(30f, 1, 2, 1);

    }

    private float[] mCurrentMatrix = //原始矩阵
            {1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1};

    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(mProjectMatrix,0,left,right,bottom,top,near,far);
    }
    //设置相机
    public void setCamera(float ex, float ey, float ez, float cx, float cy, float cz, float ux, float uy, float uz) {
        Matrix.setLookAtM(mViewMatrix,0,ex,ey,ez,cx,cy,cz,ux,uy,uz);
    }

    //平移相机
    public void translate(float x,float y,float z){
        Matrix.translateM(mCurrentMatrix,0,x,y,z);
    }

    //旋转相机
    public void rotate(float angle,float x,float y,float z){
        Matrix.rotateM(mCurrentMatrix,0,angle,x,y,z);
    }

    //缩放相机
    public void scale(float x,float y,float z){
        Matrix.scaleM(mCurrentMatrix,0,x,y,z);
    }

    public float[] getFinalMatrix(){
        float[] ans = new float[16];
        Matrix.multiplyMM(ans,0,mCurrentMatrix,0,mViewMatrix,0);
        Matrix.multiplyMM(ans,0,mProjectMatrix,0,ans,0);
        return ans;
    }











    public void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        int vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        float[] finalMatrix = getFinalMatrix();
        GLES20.glUniformMatrix4fv(vMatrix,1,false,finalMatrix,0);

        int vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition,COORDS_PER_VERTEX,GLES20.GL_FLOAT,false,vertexStride,vertexBuffer);

//        int vColor = GLES20.glGetUniformLocation(mProgram, "vColor");
//        GLES20.glUniform4fv(vColor,1,color,0);
        //获取片元着色器的vColor成员的句柄
        int mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(mColorHandle, 2, color, 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        //索引法绘制正方形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,index.length,GLES20.GL_UNSIGNED_SHORT,indexBuffer);

        GLES20.glDisableVertexAttribArray(vPosition);

    }

    public void onSurfaceCreated() {
//开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
