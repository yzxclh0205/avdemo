package com.example.av_sample.opengles;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by lh on 2018/10/29.
 */

public class MyGLSurfaceView extends GLSurfaceView{

    public MyGLSurfaceView(Context context) {
        super(context);
        //1.设置版本 2.创建GLSurfaceView.Renderer
        setEGLContextClientVersion(2);
        GLSurfaceView.Renderer render = new MyGLSurfaceViewRenderer2();
        setRenderer(render);

    }


}
