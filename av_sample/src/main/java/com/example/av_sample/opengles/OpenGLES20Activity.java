package com.example.av_sample.opengles;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.example.av_sample.PermissionManager;
import com.example.av_sample.R;
import com.example.av_sample.only264.H264Encoder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
