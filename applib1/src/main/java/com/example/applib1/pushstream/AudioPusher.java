package com.example.applib1.pushstream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.applib1.pushstream.params.AudioParam;

/**
 * Created by lh on 2018/9/30.
 */

class AudioPusher implements Pusher{
    private final AudioParam audioParam;
    private final PushNative pushNative;
    private int minBufferSize;
    private AudioRecord audioRecord;
    private boolean isCollection;

    public AudioPusher(AudioParam audioParam, PushNative pushNative) {
        this.audioParam = audioParam;
        this.pushNative = pushNative;
        initAudioRecode();
    }

    private void initAudioRecode() {
        int chanelConfig = this.audioParam.getChannel() == 1? AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO;

        //最小的缓冲区大小
        minBufferSize = AudioRecord.getMinBufferSize(audioParam.getSampleRateInHz(), chanelConfig, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,audioParam.getSampleRateInHz(),chanelConfig, AudioFormat.ENCODING_PCM_16BIT,minBufferSize);
    }

    @Override
    public void startPush() {
        isCollection = true;//这里的开始和停止不同于视频，视频的开始结束 是 处理打开预览，停止预览。 这里是直接开始录音和停止录音，采集一起做
        //启动一个录音子线程
        new Thread(new AudioRecordTask());
    }

    @Override
    public void stopPush() {
        isCollection = false;
        audioRecord.stop();
    }

    @Override
    public void release() {
        if(audioRecord!=null){
            audioRecord.release();//上面的stop会先执行
            audioRecord = null;
        }

    }

    class AudioRecordTask implements Runnable{

        @Override
        public void run() {
            audioRecord.startRecording();
            //死循环获取数据。这里反复创建数组对象是否可以优化？
            byte[] bytes = new byte[minBufferSize];
            int len = audioRecord.read(bytes, 0, bytes.length);
            if(len >0){
                pushNative.fireAudioData(bytes,len);
            }
        }
    }
}
