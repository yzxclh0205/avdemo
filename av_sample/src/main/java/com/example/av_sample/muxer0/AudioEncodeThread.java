package com.example.av_sample.muxer0;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Created by lh on 2018/10/26.
 */

public class AudioEncodeThread extends Thread{
    public static final String TAG = "AudioEncoderThread";

    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;
    private static final int audioRecordFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_COUNT = 2;
    private static final int CHANNEL_FORMAT = CHANNEL_COUNT == 1? AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO;

    private static final int BIT_RATE = 64000;
    public static final int SAMPLES_PER_FRAME = 1024;
    public static final int FRAMES_PER_BUFFER = 25;

    private final MediaCodec.BufferInfo mBufferInfo;
    private final WeakReference<MediaMuxerThread> mediaMuxer;
    private MediaCodec mMediaCodec;
    private MediaFormat audioFormat;
    private AudioRecord audioRecord;
    private static final int[] AUDIO_SOURCES = new int[]{MediaRecorder.AudioSource.DEFAULT};
    private boolean isMuxerReady;
    private boolean isStart;
    private boolean isExit;

    public AudioEncodeThread(WeakReference<MediaMuxerThread> mediaMuxerThreadWeakReference) {
        this.mediaMuxer = mediaMuxerThreadWeakReference;
        mBufferInfo = new MediaCodec.BufferInfo();
        prepare();
    }

    private void prepare() {
        MediaCodecInfo audioCodecInfo = selectAudioCodec(MIME_TYPE);
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        Log.e(TAG, "selected codec: " + audioCodecInfo.getName());
        audioFormat = MediaFormat.createAudioFormat(MIME_TYPE,SAMPLE_RATE,CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT,CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE,SAMPLE_RATE);
        Log.e(TAG, "format: " + audioFormat);
    }

    private MediaCodecInfo selectAudioCodec1(String mimeType) {
        MediaCodecInfo result = null;
        int codecCount = MediaCodecList.getCodecCount();
        Log.e("111", "selectAudioCodec。。。" + codecCount);
        for(int i=0;i<codecCount;i++){
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if(!codecInfoAt.isEncoder()){
                continue;
            }
            String[] supportedTypes = codecInfoAt.getSupportedTypes();
            for(int j=0;j<supportedTypes.length;j++){
                Log.i(TAG, "supportedType:" + codecInfoAt.getName() + ",MIME=" + supportedTypes[j]);
                if(supportedTypes[i].equalsIgnoreCase(mimeType)){
                    result = codecInfoAt;
                    break;
                }
            }
        }
        return result;

    }

    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
        MediaCodecInfo result = null;
        // get the list of available codecs
        Log.e("111", "selectAudioCodec");
        final int numCodecs = MediaCodecList.getCodecCount();
        Log.e("111", "selectAudioCodec。。。" + numCodecs);
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                Log.i(TAG, "supportedType:" + codecInfo.getName() + ",MIME=" + types[j]);
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (result == null) {
                        result = codecInfo;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void startMediaCodec() throws IOException{
        if(mMediaCodec !=null){
            return;
        }
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        prepareAudioRecord();
        isStart = true;
    }
    private void prepareAudioRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        try{
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_FORMAT, audioRecordFormat);
            int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
            if(buffer_size < minBufferSize){
                buffer_size = ((minBufferSize / SAMPLES_PER_FRAME) +1 )* SAMPLES_PER_FRAME * 2;
            }
            audioRecord = null;
            for(final int source : AUDIO_SOURCES){
                try{
                    audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,SAMPLE_RATE,CHANNEL_FORMAT,audioRecordFormat,buffer_size);
                    if(audioRecord.getState() !=AudioRecord.STATE_INITIALIZED){
                        audioRecord = null;
                    }

                }catch (Exception e){
                    audioRecord = null;
                    e.printStackTrace();
                }
                if (audioRecord != null) break;
            }


        }catch (Exception e){
            Log.e(TAG, "AudioThread#run", e);
            e.printStackTrace();
        }
    }

    private void stopMediaCodec() {
        if(audioRecord!=null){
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        try{
            Thread.sleep(100);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        if(mMediaCodec!=null){
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        isStart = false;
        Log.e("angcyo-->", "stop audio 录制...");


    }



    public void exit() {
        isExit = true;
    }

    public void restart() {
        isStart = false;
        isMuxerReady = false;
    }

    private final Object lock = new Object();
    public void setMuxerReady(boolean muxerReady) {
        synchronized (lock){
            Log.e("angcyo-->", Thread.currentThread().getId() + " audio -- setMuxerReady..." + muxerReady);
            isMuxerReady = muxerReady;
            lock.notify();
        }
    }

    @Override
    public void run() {
        super.run();
        final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        int readLen;
        while (!isExit){
            if(!isStart){
                stopMediaCodec();
                Log.e(TAG, Thread.currentThread().getId() + " audio -- run..." + isMuxerReady);

                if(!isMuxerReady){
                    synchronized (lock){
                        Log.e(TAG, "audio -- 等待混合器准备...");
                        lock.notify();
                    }
                }
                if(isMuxerReady){
                    Log.e(TAG, "audio -- startMediaCodec...");
                    try {
                        startMediaCodec();
                    } catch (IOException e) {
                        e.printStackTrace();
                        isStart = false;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }else{
                if(audioRecord!=null){
                    buf.clear();
                    readLen = audioRecord.read(buf,SAMPLES_PER_FRAME);
                    if(readLen>0){
                        buf.position(readLen);
                        buf.flip();
                        Log.e("ang-->", "解码音频数据:" + readLen);
                        try {
                            encode(buf, readLen, getPTSUs());
                        } catch (Exception e) {
                            Log.e(TAG, "解码音频(Audio)数据 失败");
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    private void encode(ByteBuffer buf, int readLen, long ptsUs) {
        if(isExit){
            return;
        }
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
         /*向编码器输入数据*/
        if(inputBufferIndex>0){
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            if(buf!=null){
                inputBuffer.put(buf);
            }
            if(readLen<0){
                Log.i(TAG, "send BUFFER_FLAG_END_OF_STREAM");
                mMediaCodec.queueInputBuffer(inputBufferIndex,0,0,ptsUs,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }else{
                mMediaCodec.queueInputBuffer(inputBufferIndex,0,readLen,ptsUs,0);
            }
        }else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // wait for MediaCodec encoder is ready to encode
            // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
            // will wait for maximum TIMEOUT_USEC(10msec) on each call
        }
        /*获取解码后的数据*/
        MediaMuxerThread mediaMuxerThread = mediaMuxer.get();
        if (mediaMuxerThread == null) {
            Log.w(TAG, "MediaMuxerRunnable is unexpectedly null");
            return;
        }
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        while (outputBufferIndex>0){
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaCodec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                final MediaFormat format = mMediaCodec.getOutputFormat(); // API >= 16
                if (mediaMuxerThread != null) {
                    Log.e(TAG, "添加音轨 INFO_OUTPUT_FORMAT_CHANGED " + format.toString());
                    mediaMuxerThread.addTrackIndex(MediaMuxerThread.TRACK_AUDIO, format);
                }

            } else if (outputBufferIndex < 0) {
                Log.e(TAG, "encoderStatus < 0");
            }
            else {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                if((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0){
                    mBufferInfo.size = 0;
                }
                if(mBufferInfo.size!=0){
                    mBufferInfo.presentationTimeUs = getPTSUs();
                    mediaMuxerThread.addMuxerData(MediaMuxerThread.TRACK_AUDIO,outputBuffer,mBufferInfo);
                    prevOutputPTSUs = mBufferInfo.presentationTimeUs;
                }
            }
            mMediaCodec.releaseOutputBuffer(outputBufferIndex,false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
        }
    }

    private long prevOutputPTSUs = 0;
    private static final int TIMEOUT_USEC = 10000;
    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    private long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }
}
