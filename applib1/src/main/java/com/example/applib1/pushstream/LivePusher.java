package com.example.applib1.pushstream;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.example.applib1.pushstream.listener.LiveStateChangeListener;
import com.example.applib1.pushstream.params.AudioParam;
import com.example.applib1.pushstream.params.VideoParam;


//直播的组织类。视频处理、音频处理、推流
//1.初始化时:1.设置surfaceHolder的生命周期回调 2.创建推送、视频、音频处理对象
            //2.直播方法：1.开始 2.暂停 。外加一个释放资源

public class LivePusher implements SurfaceHolder.Callback {

    private PushNative pushNative;
    private VideoPusher videoPusher;
    private AudioPusher audioPusher;
    private SurfaceHolder surfaceHolder;

    public LivePusher(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        prepare();

    }

    private void prepare() {
        //生命周期：销毁时 停止推流，释放资源
        this.surfaceHolder.addCallback(this);
        //c处理对象：设置音视频参数，推流
        pushNative = new PushNative();

        VideoParam videoParam = new VideoParam(480,320, Camera.CameraInfo.CAMERA_FACING_BACK);
        //视频处理应该要传递的参数：1.视频参数 2.surfaceHold 3.推流处理对象（给c设置参数，发送数据）
        videoPusher = new VideoPusher(videoParam,this.surfaceHolder,pushNative);

        AudioParam audioParam = new AudioParam();
        audioPusher = new AudioPusher(audioParam,pushNative);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPush();
        release();
    }

    public void startPush(String url, LiveStateChangeListener listener) {
        videoPusher.startPush();//这里只是开始采集
        audioPusher.startPush();
        pushNative.startPush(url);
        pushNative.setListener(listener);
    }

    public void stopPush() {
        //停止视频、音频、推流处理、移除监听
        videoPusher.stopPush();
        audioPusher.stopPush();
        pushNative.stopPush();
        pushNative.removeListener();

    }

    private void release() {
        videoPusher.release();
        audioPusher.release();
        pushNative.release();
    }

    public void switchCamera() {
        videoPusher.switchCamera();
    }
}
