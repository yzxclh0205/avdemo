package com.example.av_sample.opengles.sample;

import android.opengl.GLES20;

/**
 * Created by lh on 2018/10/29.
 */

public class ShaderUtil {

    /**
     * Shader们包含了OpenGLShading Language (GLSL)代码，必须在使用前编译。要编译这些代码，在你的Renderer类中创建一个工具类方法：
     */
    public static int loadShader(int type,String shaderCode){
        //1.创建顶点着色器的类型对象 type:GLES20.GL_VERTEX_SHADER;GLES20.GL_FRAGMENT_SHADER
        //2.给着色器 设置源代码
        //3.编译着色器
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader,shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
