package com.example.av_sample.muxer0;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Created by lh on 2018/10/26.
 */

public class VideoEncodeThread extends Thread{


    private static final String TAG = "VideoEncoderThread";
    private static int width;
    private static int height;
    private  WeakReference<MediaMuxerThread> mediaMuxer;
    private final Vector<byte[]> frameBytes;
    // 编码相关参数
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private static final int FRAME_RATE = 30; // 帧率
    private static final int IFRAME_INTERVAL = 1; // I帧间隔（GOP）
    private static final int TIMEOUT_USEC = 10000; // 编码超时时间
    private MediaCodecInfo mediaCodecInfo;
    private static final int Compress_ratio = 256;
    private static final int bit_rate = width * height * 3 * 8 * FRAME_RATE / Compress_ratio;
    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;
    private boolean isMuxerReady;
    private boolean isStart;
    private boolean isExit;
    private byte[] mFrameData;
    private MediaCodec.BufferInfo mBufferInfo;

    public VideoEncodeThread(int i, int i1, WeakReference<MediaMuxerThread> mediaMuxerThreadWeakReference) {
        this.width = i;
        this.height = i1;
        this.mediaMuxer = mediaMuxer;
        frameBytes = new Vector<byte[]>();
        prepare();
    }

    private void prepare() {
        Log.i(TAG, "VideoEncoderThread().prepare");
        mFrameData = new byte[width*height*3/2];
        mBufferInfo = new MediaCodec.BufferInfo();
        mediaCodecInfo = select(MIME_TYPE);
        if (mediaCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,width,height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 5);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,IFRAME_INTERVAL);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);

    }

    private MediaCodecInfo select(String mimeType) {
        MediaCodecInfo result = null;
        int size = MediaCodecList.getCodecCount();
        for(int i=0;i<size;i++){
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if(!codecInfoAt.isEncoder()){
                continue;
            }
            String[] types = codecInfoAt.getSupportedTypes();
            for(int j=0;j<types.length;j++){
                if(mimeType.equalsIgnoreCase(types[j])){
                    result = codecInfoAt;
                    break;
                }
            }
        }
        return result;
    }

    private final Object lock = new Object();
    @Override
    public void run() {
        super.run();
        while (!isExit){
            if(!isStart){
                stopMediaCodec();

                if(!isMuxerReady){
                    synchronized (lock){
                        try {
                            Log.e(TAG, "video -- 等待混合器准备...");
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(isMuxerReady){
                    try {
                        Log.e(TAG, "video -- startMediaCodec...");
                        startMediaCodec();
                    } catch (IOException e) {
                        isStart = false;
                        e.printStackTrace();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }else{
                if(!frameBytes.isEmpty()){
                    byte[] bytes = this.frameBytes.remove(0);
                    Log.e("ang-->", "解码视频数据:" + bytes.length);
                    try{
                        encodeFrame(bytes);
                    }catch (Exception e){
                        Log.e(TAG, "解码视频(Video)数据 失败");
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.e(TAG, "Video 录制线程 退出...");
    }

    private void encodeFrame(byte[] bytes) {
        NV21toI420SemiPlanar(bytes,mFrameData,width,height);

        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);//从队列获取可操作的buff索引
        if(inputBufferIndex > 0){
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(mFrameData);
            mediaCodec.queueInputBuffer(inputBufferIndex,0,mFrameData.length,System.nanoTime()/1000,0);
        }else{
            Log.e(TAG, "input buffer not available");
        }

        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        Log.i(TAG, "outputBufferIndex-->" + outputBufferIndex);
        while (outputBufferIndex>0){
            if(outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER){

            }
            else if(outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                outputBuffers = mediaCodec.getOutputBuffers();
            }
            else if(outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat outputFormat = mediaCodec.getOutputFormat();
                MediaMuxerThread mediaMuxerThread = mediaMuxer.get();
                if(mediaMuxerThread!=null){
                    mediaMuxerThread.addTrackIndex(MediaMuxerThread.TRACK_VIDEO, outputFormat);
                }
            }
            else if(outputBufferIndex <0){
                Log.e(TAG, "outputBufferIndex < 0");
            }
            else {
                Log.d(TAG, "perform encoding");
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                if((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0){
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }
                if(mBufferInfo.size!=0){
                    MediaMuxerThread mediaMuxerThread = mediaMuxer.get();
                    if(mediaMuxerThread!=null && !mediaMuxerThread.isVideoTrackAdd()){
                        MediaFormat outputFormat = mediaCodec.getOutputFormat();
                        mediaMuxerThread.addTrackIndex(MediaMuxerThread.TRACK_VIDEO, outputFormat);
                    }
                    outputBuffer.position(mBufferInfo.offset);
                    outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);

                    if(mediaMuxerThread!=null && mediaMuxerThread.isMuxerStart()){
                        //这里传mBufferInfo过去 会不会数据被更改
                        mediaMuxerThread.addMuxerData(MediaMuxerThread.TRACK_VIDEO,outputBuffer,mBufferInfo);
                    }
                    Log.d(TAG, "sent " + mBufferInfo.size + " frameBytes to muxer");
                }
            }
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        }

    }

    private void NV21toI420SemiPlanar(byte[] bytes, byte[] mFrameData, int width, int height) {
        int frameSize = width * height;
        System.arraycopy(bytes,0,mFrameData,0,frameSize);
        for(int i=frameSize;i<frameSize/2;i+=2){
            mFrameData[i] = bytes[i+1];
            mFrameData[i+1] = bytes[i];
        }
    }

    private void startMediaCodec() throws IOException {
        mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
//        mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        isStart = true;
    }

    private void stopMediaCodec() {
        if(mediaCodec!=null){
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        isStart = false;
        Log.e(TAG, "stop video 录制...");
    }

    public void exit() {
        isExit = true;
    }

    public void add(byte[] data) {
        if(frameBytes!=null && isMuxerReady){
            frameBytes.add(data);
        }
    }

    public synchronized void restart() {
        isStart = false;
        isMuxerReady = false;
        frameBytes.clear();
    }

    public void setMuxerReady(boolean muxerReady) {
        synchronized (lock){
            Log.e(TAG, Thread.currentThread().getId() + " video -- setMuxerReady..." + muxerReady);
            isMuxerReady = muxerReady;
            lock.notify();
        }
    }
}
