package com.example.av_sample.opengles.study.base;

import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by lh on 2018/10/31.
 */

public class BaseGLSL {
    private static final String TAG = "BaseGLSL";
    public static final int COORDS_PER_VERTEX = 3;//每个顶点的坐标数
    public static final int vertexStride = COORDS_PER_VERTEX * 4;//每个顶点 4个字节

    //加载着色器 ：着色器类型、着色器代码
    public static int loadShader(int type,String shaderCode){
        //1.根据类型创建顶点着色器或 片元着色器
        int shader = GLES20.glCreateShader(type);
        //2.将着色器代码添加到着色器中
        GLES20.glShaderSource(shader,shaderCode);
        //3.编译着色器
        GLES20.glCompileShader(shader);
        return shader;
    }

    //生成OPENGL Program：顶点着色器代码、片元着色器代码。 如果结果是0表示失败
    public int createOpenGLProgram(String vertexSource,String fragmentSource){
        //1.创建顶点着色器、片元着色器。 为0表示创建
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if(vertex == 0){
            Log.e(TAG, "loadShader vertex failed");
            return 0;
        }
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        if(fragment == 0){
            Log.e(TAG, "loadShader fragment failed");
            return 0;
        }
        //创建program
        int program = GLES20.glCreateProgram();
        if(program!=0){
            GLES20.glAttachShader(program,vertex);
            GLES20.glAttachShader(program,fragment);
            GLES20.glLinkProgram(program);
            //查看是否链接成功
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linkStatus,0);
            if(linkStatus[0]!=GLES20.GL_TRUE){
                Log.e(TAG, "Could not link program:" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

}
