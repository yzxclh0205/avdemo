package com.example.av_sample.opengles.sample;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by lh on 2018/10/29.
 */

public class MyGLSurfaceView extends GLSurfaceView{

    private final MyGLSurfaceViewRenderer3 render;

    public MyGLSurfaceView(Context context) {
        super(context);
        //1.设置版本 2.创建GLSurfaceView.Renderer
        setEGLContextClientVersion(2);
        render = new MyGLSurfaceViewRenderer3();
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    private float mPreviousX;
    private float mPreviousY;
    private final float TOUCH_SCALE_FACTOR = 180.0f/ 320;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                if(y>getHeight() /2){
                    dx = dx * -1;
                }
                if(x <getWidth()/2){
                    dy = dy * -1;
                }
                render.setAngle(render.getAngle() + ((dx + dy)* TOUCH_SCALE_FACTOR));
                requestRender();
        }
        mPreviousX = x;
        mPreviousY = y;

        return true;
    }
}
