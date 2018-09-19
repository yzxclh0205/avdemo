package com.example.app1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.applib1.JniFFmpegConfig;
import com.example.applib1.JniTest;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {


    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tx = findViewById(R.id.tx_content);
        test1();
        test3();
//
//        test2();


    }

    private void test3() {

        //new SurfaceView(this);
        surfaceView = findViewById(R.id.surfaceView);
//        SurfaceHolder holder = surfaceView.getHolder();
//        holder.setFormat(PixelFormat.RGBA_8888);

        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                JniTest jniTest = new JniTest();
                Surface surface = surfaceView.getHolder().getSurface();
                String video = "Forrest_Gump_IMAX.mp4";
                String input = new File(Environment.getExternalStorageDirectory(),video).getAbsolutePath();
//                jniTest.render(input,surface);
                String output = new File(Environment.getExternalStorageDirectory(),"lhtest.pcm").getAbsolutePath();
//                jniTest.sound(input, output);
                jniTest.play(input, output,surface);
            }
        });
//        ViewGroup viewGroup = findViewById(R.id.rootView);
//        viewGroup.addView(surfaceView,0);

    }

    private void test2() {
//        JniFFmpegConfig jniFFmpegConfig = new JniFFmpegConfig();
//        jniFFmpegConfig.startTest();
    }

    private void test1() {
        JniTest jniTest = new JniTest();
        try {
//            jniTest.startTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

}
