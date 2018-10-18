package com.example.av_sample.util;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lh on 2018/10/15.
 */

public class PcmToWavUtil {

    //音频有关的三个参数 。采样率、采样格式、声道格式（关联声道个数）
    private int sampleRateInHz;
    private int audioFmt;
//    private int channelFmt;
    private int channels;
    private int minBuffSize;

    public PcmToWavUtil(int sampleRateInHz, int sampleFmt, int channels) {
        this.sampleRateInHz = sampleRateInHz;
        this.audioFmt = sampleFmt;//AudioFormat.ENCODING_PCM_16BIT
//        this.channelFmt = channelFmt;
        this.channels = channels;
        int channelFmt = channels == 1? AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO;
        minBuffSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelFmt,audioFmt);
        minBuffSize = 16 * sampleRateInHz * channels / 8;
    }



    /**
     * pcm文件转wav文件
     * @param inFileName
     * @param outFileName
     */
    public void writeWave(String inFileName,String outFileName){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(inFileName);
            fos = new FileOutputStream(outFileName);
            //获取输入文件的长度
            long size = fis.getChannel().size();
            writeWaveHeader(fos,size);
            byte[] bytes = new byte[minBuffSize];
            int len = 0;
            while((len = fis.read(bytes))!=-1){
                fos.write(bytes,0,len);
            }
            fis.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 加入 wav头 44个字节
     * 一个个操作字节的处理.版本 。字节数组的赋值还可以使用System.arraycopy处理
     */
    private void writeWaveHeader(FileOutputStream fos,long audioLen) throws IOException {
        //c中对应三个结构体： wave_header 、wave_fmt、wave_data. 注意c中默认小端，java中默认大端
        byte[] header = new byte[44];
        //1.wave_header 12个字节。byte[4]、int、byte[4]。对应fccID，整个文件长度、fccType
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        //java 处理成小端 4个字节。
        long totalLen = audioLen + 36;
        header[4] = (byte)(totalLen &0xff);
        header[5] = (byte)((totalLen >>8)&0xff);
        header[6] = (byte)((totalLen >>16)&0xff);
        header[7] = (byte)((totalLen >>24)&0xff);
        //字节数组的赋值可以使用System.arraycopy处理
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //2.wave_fmt 24个字节。 byte[4]、int、short、short、int、int、short、short
        //分别对应的是fccID-4、dwSize-4即采样位数、wFormatTag-2 、wChannels-2即声道格式、dwSamplesPerSec采样率、
        // dwAvgBytesPerSec即采样码数（公式：采样位数（采样格式） * 声道数 * 采样率 /8 --字节）
        //wBlockAlign、uiBitsPerSample 这个跟dwSize是啥关系-----------------------------------------------------？？；
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        //dwSize 4字节  // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //wFormatTag
        header[20] = 1;
        header[21] = 0;
        //wChannel 小端
        header[22] = (byte) channels;
        header[23] = 0;
        //dwSamplesPerSec 小端
        header[24] = (byte)(sampleRateInHz &0xff);
        header[25] = (byte)((sampleRateInHz >>8)&0xff);
        header[26] = (byte)((sampleRateInHz >>16)&0xff);
        header[27] = (byte)((sampleRateInHz >>24)&0xff);
        //dwAvgBytesPerSec即采样码数（公式：采样位数（采样格式） * 声道数 * 采样率 /8 --字节） 4个字节
        int dwAvgBytesPerSec = 16 * channels * sampleRateInHz /2;
        header[28] = (byte)(dwAvgBytesPerSec &0xff);
        header[29] = (byte)((dwAvgBytesPerSec >>8)&0xff);
        header[30] = (byte)((dwAvgBytesPerSec >>16)&0xff);
        header[31] = (byte)((dwAvgBytesPerSec >>24)&0xff);
        //wBlockAlign 2字节 // block align
        header[32] = (byte)( 2 * 16 /8);//这里是 channels * fmt / 8 ？
        header[33] = 0;
        //uiBitsPerSample  // bits per sample 声道的采样格式
        header[34] = 16;
        header[35] = 0;
        // data byte[4]、int 即1.fccID dwSize
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte)(audioLen &0xff);
        header[41] = (byte)((audioLen >>8)&0xff);
        header[42] = (byte)((audioLen >>16)&0xff);
        header[43] = (byte)((audioLen >>24)&0xff);
        fos.write(header);
    }

}
