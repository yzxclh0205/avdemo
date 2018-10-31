package com.example.av_sample.opengles.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by lh on 2018/10/19.
 */

public class OpenGLES20Activity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyGLSurfaceView myGLSurfaceView = new MyGLSurfaceView(this);
        setContentView(myGLSurfaceView);
        test1();
    }

    private void test1() {

    }
}
