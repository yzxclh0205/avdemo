package com.example.applib1;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class JniTest {

    static{
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("postproc");
        System.loadLibrary("avfilter");
        System.loadLibrary("lh-jni-lib-1");
    }

    //jni申明 native方法1：java静态方法从c代码native层获取字符串（返回）
    public native static String getStrFromC();
    //jni申明 native方法2：java对象方法从c代码native层获取字符串（返回），传递int （传参）
    public native String getStrFromC(int i);
    public native String getStrFromC(int i,String j);
    //jni申明 native方法3：c代码native层访问java层的成员属性-：访问属性，返回修改之后的内容
    public native String accessField();// 逻辑，java层调c代码，然后c去哪java层的属性
    //jni申明 native方法4：c代码native层访问java层的 Class的静态属性
    public native String accessStaticField();
    //jni申明 native方法5：c代码native层访问java层的 成员方法
    public native int accessMethod();
    //jni申明 native方法6：c代码native层访问java层的 Class的静态方法
    public native String accessStaticMethod();
    //jni申明 native方法7：c代码native层 创建java层对象（通过访问方法-构造方法）
    public native Date getDate();
    //jni申明 native方法8：c代码获得java层属性对象，并调用属性对象的父类的方法
    public native String callNonvirtualMethod();
    //jni申明 native方法8：java传递中文给c，处理后并返回，防止乱码。都采用GB2312-----------实践发现并不用转码
    public native String chineseChars(String str);

    public native String chineseChars1(byte bytes[]); //中文问题先放着

    public native int[] giveArray(int[] array);

    //c返回指定大小长度的int数组
    public native int[] getArray(int len);
    public native String[] getArray1(int len);

    // c中处理局部 Local Reference
    public native void localRef();

    public native void createGlobalRef();
    public  native String getGlobalRef();
    public native void deleteGlobalRef();

    public native void exception();

    public native int cached();

    public native void ndkfilecrypt_crypt(String in,String out);

    public native void ndkfilecrypt_decrypt(String in,String out);

    public native void logFFmpegConfig(String url);

    public native void render(String input,Surface surface);




    public String key = "lhtest_jni";
    public static int count = 9;
    private Human human = new Man();

    public void startTest() throws UnsupportedEncodingException {
        String strFromC = getStrFromC();
        log("strFromC",strFromC);
        String strFromC1 = getStrFromC(1);
        log("strFromC1",strFromC1);
        String s = accessField();
        log("accessField",s);

        s = accessStaticField();
        log("accessStaticField",""+count);

        int i = accessMethod();
        log("accessMethod","xx"+i);

        String s1 = accessStaticMethod();
        log("accessStaticMethod",""+s1);

        Date date = getDate();
        log("getDate",""+date.getTime());
        String s2 = callNonvirtualMethod();
        log("getDate",""+s2);

        String s3 = chineseChars("我是java层的中文");
        log("chineseChars",""+s3);

        String s4 = chineseChars1("我是中文哦，java层的".getBytes("GB2312"));

        int[] array = {3,1,9,5,7,2,3};
        int[] i1 = giveArray(array);
//        List<int[]> ints = Arrays.asList(i1);
//        ints.add(array);
        for(i=0;i<array.length;i++){
            log("giveArray",array[i]+"" +i1[i]);
        }

        int[] array1 = getArray(5);
        for(i=0;i<array1.length;i++){
            log("getArray",array1[i]+"");
        }
        String[] array11 = getArray1(5);
        for(i=0;i<array11.length;i++){
            log("getArray1",array11[i]+"");
        }

        localRef();

        createGlobalRef();
        String globalRef = getGlobalRef();
        log("getGlobalRef",globalRef+"");
        deleteGlobalRef();
        globalRef = getGlobalRef();
        log("getGlobalRef",globalRef+"");

        try{
            exception();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            exception();
        }catch (Exception e){
            e.printStackTrace();
        }

        int cached = cached();
        log("cached",cached+"");
         cached = cached();
        log("cached",cached+"");
         cached = cached();
        log("cached",cached+"");
        testFile();

        logFFmpegConfig("");
        log("logFFmpegConfig",cached+"");
    }

    private void testFile() {
        String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        log("absolutePath",absolutePath+"");
        String path = Environment.getExternalStorageDirectory().getPath();
        log("path",path+"");
        File inFile = new File(absolutePath+File.separator+"lhjni.txt");
        File inFile1 = new File(absolutePath+File.separator+"lhjni2.txt");
        File outFile = new File(absolutePath+File.separator+"lhjni1.txt");
//        if(!inFile.exists()){
//            inFile.mkdir();
//        }
        try {
//            inFile.createNewFile();
//            inFile1.createNewFile();
//            outFile.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile));
            bufferedWriter.write("AAA");
            bufferedWriter.flush();
            bufferedWriter.close();

            ndkfilecrypt_crypt(outFile.getAbsolutePath(),inFile.getAbsolutePath());
            ndkfilecrypt_crypt(inFile.getAbsolutePath(),inFile1.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void log(String methodName,String text){
        Log.e("JniTest "+methodName,""+text);
    }

    //产生指定范围的随机数
    public int getRandomInt(int max){
        System.out.println("genRandomInt 执行了");
        return new Random().nextInt(max);
    }

    //产生UUID字符串
    public static String getUUID(){
        return UUID.randomUUID().toString();
    }

    public AudioTrack getAudioTrack(int sampleRateInHz,int nb_channels){

        int channleConfig;
        if(nb_channels == 1){
            channleConfig = AudioFormat.CHANNEL_IN_MONO;
        }else{
            channleConfig = AudioFormat.CHANNEL_IN_STEREO;
        }
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        int buffSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz,channleConfig,audioFormat);

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRateInHz,channleConfig,audioFormat,buffSizeInBytes,AudioTrack.MODE_STREAM);
//        audioTrack.play();
//        audioTrack.write(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes
        return audioTrack;
    }

    public native void sound(String input,String output);
}