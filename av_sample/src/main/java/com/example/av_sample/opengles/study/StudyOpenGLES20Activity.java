package com.example.av_sample.opengles.study;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.av_sample.opengles.sample.MyGLSurfaceView;
import com.example.av_sample.opengles.study.glsv.SquareSurfaceView;
import com.example.av_sample.opengles.study.glsv.TriangleGLSurfaceView;

/**
 * Created by lh on 2018/10/19.
 */

public class StudyOpenGLES20Activity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //三角形
//        setContentView(new TriangleGLSurfaceView(this));
        //正方形
        setContentView(new SquareSurfaceView(this));
    }
}
