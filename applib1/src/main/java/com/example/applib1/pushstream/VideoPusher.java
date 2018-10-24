package com.example.applib1.pushstream;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.example.applib1.pushstream.params.VideoParam;

//1.

class VideoPusher implements SurfaceHolder.Callback,Pusher, Camera.PreviewCallback {

    private final VideoParam videoParam;
    private final SurfaceHolder surfaceHolder;
    private final PushNative pushNative;
    private Camera mCamera;
    private byte[] bytes;
    private boolean isCollection;

    public VideoPusher(VideoParam videoParam, SurfaceHolder surfaceHolder, PushNative pushNative) {
        this.videoParam = videoParam;
        this.surfaceHolder = surfaceHolder;
        this.pushNative = pushNative;
        this.surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview();
    }

    //开始预览：1.open摄像头id 2.获取参数，并设置预览尺寸、缓存、像素格式
    private void startPreview() {
        try{
            mCamera = Camera.open(videoParam.getCameraId());
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);

            parameters.setPictureSize(videoParam.getWidth(),videoParam.getHeight());
//            parameters.setPreviewFrameRate();
            //parameters.setPreviewFpsRange(videoParams.getFps()-1, videoParams.getFps());
            //将处理的参数设置到Camera中：后置摄像头、像素格式、预览的尺寸大小
            mCamera.setParameters(parameters);

            //获取预览图像素数据：需要添加缓存
            bytes = new byte[videoParam.getWidth() * videoParam.getHeight() * 4];
            mCamera.addCallbackBuffer(bytes);
            mCamera.setPreviewCallbackWithBuffer(this);//-------------这一句开始会接收到回到
            camera.setPreviewDisplay(surfaceHolder);
            //开启预览
            mCamera.startPreview();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void startPush() {
        //开始采集：1.给c处理设置视频参数 2.设置标志位，可以采集
        pushNative.setVideoOptions(videoParam.getWidth(),videoParam.getHeight(),videoParam.getBitrate(),videoParam.getFps());
        isCollection = true;
    }

    @Override
    public void stopPush() {
        isCollection = false;//这里并不停止预览，只是不再采集数据
    }

    @Override
    public void release() {
        //这里要停止采集数据，停止预览
        stopPreview();
    }

    private void stopPreview(){
        try{
            if(mCamera!=null){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(mCamera !=null){
            mCamera.addCallbackBuffer(bytes);
        }
        if(isCollection){
            pushNative.fireVideoData(bytes);
        }
    }

    public void switchCamera() {
        if(videoParam.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK){
            videoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }else if(videoParam.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT){
            videoParam.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        //重新预览
        stopPreview();
        startPreview();
    }
}
