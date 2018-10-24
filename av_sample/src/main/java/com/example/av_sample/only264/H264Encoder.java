package com.example.av_sample.only264;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 1.硬件编码器初始化
 *      宽、高、帧率。 设置码率、帧率、图片原始格式、帧间间隔
 * 2.硬件编码器编码
 */

public class H264Encoder {

    private long timeoutUs = 12000;
    private boolean isRunning;
    private int width = 320;
    private int height = 240;
    private int frameRate = 25;

    private ArrayBlockingQueue<byte[]> yuv420Queue = new ArrayBlockingQueue<byte[]>(10);
    private MediaCodec mediaCodec;
    private BufferedOutputStream bos;
    private byte[] configbyte;

    public H264Encoder(int width, int height, int frameRate) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        //创建编码器格式对象，指定文件格式，宽，高
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",width,height);
        //指定原始图片类型
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);

        //创建编码器
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");//使用这个会抱错
            mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            //开始编码
            mediaCodec.start();
            createFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/only264.mp4";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void pushData(byte[] data){
        if(yuv420Queue.size()>10){
            yuv420Queue.poll();
        }
        yuv420Queue.add(data);
    }
    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                startEncode();
            }
        }).start();
    }

    /**
     * 开始编码
     */
    public void startEncode(){
        isRunning = true;
        //1.数据包序号 2.对应的时间戳
        byte[] input = null;
        long pts = 0;
        long generateIndex = 0;

        while(isRunning){
            //1.处理nv21数据转yuv数据
            //2.从编码数据的输入队列中拿buffer 填充要编码的数据，输入流入队列
            //3.从编码数据的输出队列中拿buffer，取要编码的数据，进行编码
                //编码区分配置信息、同步帧、普通帧
            if(yuv420Queue.size()>0){
                input = yuv420Queue.poll();
                byte[] yuv420 = new byte[width * height * 3/2];
                NV21TOYUV420(input,yuv420);
                input = yuv420;
            }
            if(input!=null){
                try {
                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                //从编码数据输入队列中取buff索引
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                if(inputBufferIndex>=0){
                    //计算时间戳
                    pts = computePresentaionTime(generateIndex);
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    inputBuffer.put(input);
                    //输入流 入读列
                    mediaCodec.queueInputBuffer(inputBufferIndex,0,input.length,System.currentTimeMillis(),0);
                    generateIndex +=1;
                }
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                //取数据
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs);
                //循环从输出编码数据中拿数据
                while(outputBufferIndex>=0){
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    byte[] outdata = new byte[bufferInfo.size];
                    //从输出编码队列中 拿一部分数据
                    outputBuffer.get(outdata);
                    if(bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG){
//                        byte[] configbyte = new byte[bufferInfo.size];
                        configbyte = outdata;
                    }else if(bufferInfo.flags == MediaCodec.BUFFER_FLAG_SYNC_FRAME){
                        byte[] keyFrame = new byte[bufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte,0,keyFrame,0,configbyte.length);
                        System.arraycopy(outdata,0,keyFrame,configbyte.length,outdata.length);
                        bos.write(keyFrame,0,keyFrame.length);
                    }else {
                        bos.write(outdata,0,outdata.length);
                    }
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs);

                }
                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }else{
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
        try {
            mediaCodec.stop();
            mediaCodec.release();
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        isRunning = false;
    }
    /**
     * 不知道这是啥意思
     */
    private long computePresentaionTime(long generateIndex) {
        return 132 * generateIndex * 1000 * 1000 / frameRate;
    }

    private void NV21TOYUV420(byte[] input, byte[] yuv420) {
        int len = width * height;
        int halfLen = len /2;
        System.arraycopy(input,0,yuv420,0,len);
        //nv21 对应的是 y4 v1 u
        for(int i=0;i<halfLen;i += 2){
            yuv420[len + i] = input[len+ i+1];
            yuv420[len + i +1] = input[len +i];
        }
    }


}
