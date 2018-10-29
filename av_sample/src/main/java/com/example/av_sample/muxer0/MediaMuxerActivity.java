package com.example.av_sample.muxer0;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.av_sample.R;

/**
 * Created by lh on 2018/10/26.
 */

public class MediaMuxerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private SurfaceView sv;
    private boolean isStart;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carema_test);
        sv = findViewById(R.id.sv);
        test1();
        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = !isStart;
                if(isStart){
                    start();
                }else{
                    stop();
                }
            }
        });
    }

    private void start(){
        startCarema();
        MediaMuxerThread.startMuxer();
    }
    private void stop(){
        stopCamera();
        MediaMuxerThread.stopMuxer();
    }

    private void startCarema() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPictureSize(320,240);
            parameters.setPreviewSize(320,240);
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            byte[] bytes = new byte[320 * 240 * 4];
            camera.addCallbackBuffer(bytes);
            camera.setPreviewDisplay(sv.getHolder());
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stopCamera() {
        if(camera!=null){
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
            camera.release();
            camera = null;
        }
    }



    private void test1() {
        sv.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(camera!=null){
            camera.addCallbackBuffer(data);
        }
        MediaMuxerThread.addVideoFrameData(data);
    }
}
