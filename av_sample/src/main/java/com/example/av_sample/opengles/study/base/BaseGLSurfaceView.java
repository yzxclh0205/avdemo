package com.example.av_sample.opengles.study.base;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by lh on 2018/10/31.
 */

public class BaseGLSurfaceView extends GLSurfaceView {


    public BaseGLSurfaceView(Context context) {
        super(context);
        initEGLContext();
    }

    public BaseGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEGLContext();
    }


    private void initEGLContext() {
        setEGLContextClientVersion(2);
    }

}
