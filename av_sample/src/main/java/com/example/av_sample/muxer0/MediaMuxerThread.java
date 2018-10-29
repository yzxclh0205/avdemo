package com.example.av_sample.muxer0;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Created by lh on 2018/10/26.
 */

public class MediaMuxerThread extends Thread {

    private static final String TAG = "MediaMuxerThread";
    private static MediaMuxerThread mediaMuxerThread;
    private Vector<MuxerData> muxerDatas;
    private FileUtils fileSwapHelper;
//    private AudioEncodeThread audioThread;
    private VideoEncodeThread videoThread;
    private boolean isExit;
    private volatile boolean isVideoTrackAdd;
    private volatile boolean isAudioTrackAdd;
    private MediaMuxer mediaMuxer;
    private final Object lock = new Object();
    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;

    private MediaMuxerThread(){

    }


    public static void startMuxer() {
        if (mediaMuxerThread == null) {
            synchronized (MediaMuxerThread.class) {
                if (mediaMuxerThread == null) {
                    mediaMuxerThread = new MediaMuxerThread();
                    Log.e("111", "mediaMuxerThread.start();");
                    mediaMuxerThread.start();
                }
            }
        }
    }

    public static void stopMuxer() {
        if(mediaMuxerThread!=null){
            mediaMuxerThread.exit();//设置音视频 两个线程中断，把推出标志置为true
            try{
                mediaMuxerThread.readyStop();
                mediaMuxerThread.join();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            mediaMuxerThread = null;
        }
    }

    private void exit() {
        if(videoThread!=null){
            videoThread.exit();
            try{
                videoThread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
//        if(audioThread!=null){
//            audioThread.exit();
//            try{
//                audioThread.join();
//            }catch (InterruptedException e){
//                e.printStackTrace();
//            }
//        }
        isExit = true;
        synchronized (lock){
            lock.notify();
        }
    }

    public static void addVideoFrameData(byte[] data) {
        if(mediaMuxerThread!=null){
            mediaMuxerThread.addVideoData(data);
        }
    }

    private void addVideoData(byte[] data) {
        if (videoThread != null) {
            videoThread.add(data);
        }

    }

    /**
     * 当前是否添加了音轨
     *
     * @return
     */
    public boolean isAudioTrackAdd() {
        return isAudioTrackAdd;
    }

    /**
     * 当前是否添加了视频轨
     *
     * @return
     */
    public boolean isVideoTrackAdd() {
        return isVideoTrackAdd;
    }

    /**
     * 当前音视频合成器是否运行了
     *
     * @return
     */
    public boolean isMuxerStart() {
        return isAudioTrackAdd && isVideoTrackAdd;
    }


    public static final int TRACK_VIDEO = 0;
    public static final int TRACK_AUDIO = 1;
    @Override
    public void run() {
        super.run();
        //初始化混合器
        initMuxer();
        while (!isExit){
            if(isMuxerStart()){
                if(muxerDatas.isEmpty()){
                    synchronized (lock){
                        try{
                            //没有数据就等待数据
                            Log.e(TAG, "等待混合数据...");
                            lock.wait();
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }

                }else{
                    if(fileSwapHelper.requestSwapFile()){
                        //需要切换文件
                        String nextFileName = fileSwapHelper.getNextFileName();
                        Log.e(TAG, "正在重启混合器..." + nextFileName);
                        restart(nextFileName);
                    }else{
                        MuxerData data = muxerDatas.remove(0);
                        Log.e(TAG, "写入混合数据 " + data.bufferInfo.size);
                        int track = 0;
                        if(data.trackIndex == TRACK_VIDEO){
                            track = videoTrackIndex;
                        }
                        else if(data.trackIndex == TRACK_AUDIO){
                            track = audioTrackIndex;
                        }
                        mediaMuxer.writeSampleData(track,data.byteBuffer,data.bufferInfo);
                    }
                }
            }else{
                synchronized (lock){
                    try {
                        Log.e(TAG, "等待音视轨添加...");
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "addTrack 异常:" + e.toString());
                    }
                }
            }
        }
        readyStop();
        Log.e(TAG, "混合器退出...");
    }

    private void restart(String filePath) {
        restartAudioVidieo();
        readyStop();
        try {
            readyStart(filePath);
        } catch (Exception e) {
            Log.e(TAG, "readyStart(filePath, true) " + "重启混合器失败 尝试再次重启!" + e.toString());
            restart();
            return;
        }
        Log.e(TAG, "重启混合器完成");
    }

    private void restartAudioVidieo() {
//        if(audioThread!=null){
////            audioTrackIndex = -1;
//            isAudioTrackAdd = false;
//            audioThread.restart();
//        }
        if(videoThread!=null){
//            vidoTrackIndex = -1;
            isVideoTrackAdd = false;
            videoThread.restart();
        }
    }

    private synchronized void readyStop(){
        if(mediaMuxer!=null){
            try {
                mediaMuxer.stop();
            } catch (Exception e) {
                Log.e(TAG, "mediaMuxer.stop() 异常:" + e.toString());
            }
            try {
                mediaMuxer.release();
            } catch (Exception e) {
                Log.e(TAG, "mediaMuxer.release() 异常:" + e.toString());

            }
            mediaMuxer = null;
        }
    }


    private void restart() {
        fileSwapHelper.requestSwapFile(true);
        String nextFileName = fileSwapHelper.getNextFileName();
        restart(nextFileName);
    }

    /**
     * 1.存储数据的集合（线程安全）
     * 2.文件对象，拿路径用
     * 3.音频编码线程处理 ，开启线程
     * 4.视频编码线程处理 ，开启线程
     * 5.开始音视频混合
     */
    private void initMuxer() {
        muxerDatas = new Vector<>();
        fileSwapHelper = new FileUtils();
//        audioThread = new AudioEncodeThread(new WeakReference<MediaMuxerThread>(this));
        videoThread = new VideoEncodeThread(320,240,new WeakReference<MediaMuxerThread>(this));
//        audioThread.start();
        videoThread.start();
        try{
            readyStart();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readyStart() {
        fileSwapHelper.requestSwapFile(true);
        readyStart(fileSwapHelper.getNextFileName());
    }

    private void readyStart(String path) {
        isExit = false;
        isVideoTrackAdd = false;
        isAudioTrackAdd = false;
        muxerDatas.clear();

        try {
            mediaMuxer = new MediaMuxer(path,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//            if(audioThread!=null){
//                audioThread.setMuxerReady(true);
//            }
            if(videoThread!=null){
                videoThread.setMuxerReady(true);
            }
            Log.e(TAG, "readyStart(String filePath, boolean restart) 保存至:" + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTrackIndex(int trackIndex, MediaFormat outputFormat) {
        if(isMuxerStart()){
            return;
        }
        if(trackIndex==TRACK_AUDIO && isVideoTrackAdd() || trackIndex == TRACK_AUDIO && isAudioTrackAdd()){
            return;
        }
        if(mediaMuxer!=null){
            int track = 0;
            track = mediaMuxer.addTrack(outputFormat);
            if(trackIndex==TRACK_AUDIO){
                audioTrackIndex = track;
                isAudioTrackAdd = true;
                Log.e(TAG, "添加音轨完成");
            }
            else{
                videoTrackIndex = track;
                isVideoTrackAdd = true;
                Log.e(TAG, "添加视频轨完成");
            }

            requestStart();
        }

    }

    /**
     * 请求混合器开始启动
     */
    private void requestStart() {
        synchronized (lock){
            if(isMuxerStart()){
                mediaMuxer.start();
                Log.e(TAG, "requestStart启动混合器..开始等待数据输入...");
                lock.notify();
            }
        }

    }

    public void addMuxerData(int trackIndex, ByteBuffer outputBuffer, MediaCodec.BufferInfo mBufferInfo) {
        if (!isMuxerStart()) {
            return;
        }
        muxerDatas.add(new MuxerData(trackIndex,outputBuffer,mBufferInfo));
        synchronized (lock){
            lock.notify();
        }
    }

    //封装需要传输的数据类型
    private static class MuxerData {
        int trackIndex;// 音视频索引
        ByteBuffer byteBuffer;//输入、输出数据的容器
        MediaCodec.BufferInfo bufferInfo;//编码器 容器信息对象，一次编码buff大小等信息

        public MuxerData(int trackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
            this.trackIndex = trackIndex;
            this.byteBuffer = byteBuffer;
            this.bufferInfo = bufferInfo;
        }
    }
}
