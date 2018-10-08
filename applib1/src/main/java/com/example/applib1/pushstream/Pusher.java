package com.example.applib1.pushstream;

//音频、视频的处理接口
public interface Pusher {
    public abstract void startPush();//区别于推流处理，音视频处理 这里是开始采集数据

    public abstract void stopPush();//区别于推流处理，音视频处理 这里是停止采集数据

    public abstract void release();//区别于推流处理，音视频处理 这里是停止预览，释放相机资源
}
