package com.example.av_sample.collectvcameradata;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.example.av_sample.PermissionManager;
import com.example.av_sample.R;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lh on 2018/10/19.
 */

public class SurfaceCameraTest extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, TextureView.SurfaceTextureListener {

    private SurfaceView sv;
    private Camera camera;
    private TextureView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carema_test);
        sv = findViewById(R.id.sv);
        tv = findViewById(R.id.tv);
        test1();
        PermissionManager permissionManager = new PermissionManager(this);
    }

    private void test1() {
        sv.getHolder().addCallback(this);
        tv.setSurfaceTextureListener(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(holder);
    }

    private void startPreview(SurfaceHolder holder) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters parameters = camera.getParameters();
        //设置图像格式 、 预览、图片尺寸 、数据回调设置、 数据回显
        parameters.setPreviewFormat(ImageFormat.NV21);
        Camera.Size optimalPreviewSize = getOptimalPreviewSize();
        parameters.setPreviewSize(320,240);
        parameters.setPictureSize(320,240);
        camera.setParameters(parameters);

        camera.setDisplayOrientation(90);
        byte[] bytes = new byte[480 * 320 * 4];
        camera.addCallbackBuffer(bytes);
        camera.setPreviewCallbackWithBuffer(this);
        try {
            camera.setPreviewDisplay(holder);
//            camera.setPreviewTexture(this.surface);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview(){
        if(camera!=null){
            camera.stopPreview();
        }
    }
    public void release(){
        stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(camera !=null){
            camera.addCallbackBuffer(data);
        }
    }
    SurfaceTexture surface;
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = surface;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private Camera.Size getOptimalPreviewSize() {
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        int width = sv.getWidth();
        int height = sv.getHeight();
//        if (DisplayUtils.getScreenOrientation(this) == Configuration.ORIENTATION_PORTRAIT) {
//            int portraitWidth = h;
//            h = w;
//            w = portraitWidth;
//        }

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = 0;
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.height - rhs.height;
            }
        });

//        int orientation = DisplayUtils.getScreenOrientation(this);
//        int width;
//        int height;
//        if(mSquareViewFinder) {
//            if(orientation != Configuration.ORIENTATION_PORTRAIT) {
//                height = (int) (getHeight() * DEFAULT_SQUARE_DIMENSION_RATIO);
//                width = height;
//            } else {
//                width = (int) (getWidth() * DEFAULT_SQUARE_DIMENSION_RATIO);
//                height = width;
//            }
//        } else {
//            if(orientation != Configuration.ORIENTATION_PORTRAIT) {
//                height = (int) (getHeight() * LANDSCAPE_HEIGHT_RATIO);
//                width = (int) (LANDSCAPE_WIDTH_HEIGHT_RATIO * height);
//            } else {
//                width = (int) (getWidth() * PORTRAIT_WIDTH_RATIO);
//                height = (int) (PORTRAIT_WIDTH_HEIGHT_RATIO * width);
//            }
//        }
//
//        if(width > getWidth()) {
//            width = getWidth() - MIN_DIMENSION_DIFF;
//        }
//        if(height > getHeight()) {
//            height = getHeight() - MIN_DIMENSION_DIFF;
//        }

        for (Camera.Size size : sizes) {
            if (size.height > height && size.width > width) {
                optimalSize = size;
                break;
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
