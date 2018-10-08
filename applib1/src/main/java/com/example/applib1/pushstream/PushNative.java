package com.example.applib1.pushstream;

import com.example.applib1.pushstream.listener.LiveStateChangeListener;

//异常相关：1.回调三步：  1.1.属性-处理对象 1.2设置对象 1.3调用处理对象
//采集处理相关：
//1.视频：1.1设置视频参数 1.2发送视频数据
//2.音频：1.1设置音频参数 1.2发送音频数据
//推流处理
//1.开始推流  2.关闭推流 3.释放资源
public class PushNative {

    //异常相关：1.回调三步：  1.1.属性-处理对象 1.2设置对象 1.3调用处理对象
    private LiveStateChangeListener listener;

    public void setListener(LiveStateChangeListener listener) {
        this.listener = listener;
    }

    public void removeListener(){
        listener = null;
    }
    /**
     * 接收Native层抛出的错误
     * @param code
     */
    public void throwNativeError(int code){
        if(listener != null){
            listener.onError(code);
        }
    }

    //采集处理相关：
        //1.视频：1.1设置视频参数 1.2发送视频数据
            //设置宽、高、比特率、帧率
    public native void setVideoOptions(int width,int height,int bitrate,int fps);
    public native void fireVideoData(byte[] data);

        //2.音频：1.1设置音频参数 1.2发送音频数据
    public native void setAudioOptions(int sampleRateInHz,int channel);
    public native void fireAudioData(byte[] data,int len);
    //推流处理
        //1.开始推流  2.关闭推流 3.释放资源
    public native void startPush(String url);
    public native void stopPush();
    public native void release();
}
