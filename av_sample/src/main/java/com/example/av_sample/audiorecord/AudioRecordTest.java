package com.example.av_sample.audiorecord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.av_sample.R;
import com.example.av_sample.util.PcmToWavUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//实现Android录音的流程为：
//        1.构造一个AudioRecord对象，其中需要的最小录音缓存buffer大小可以通过getMinBufferSize方法得到。如果buffer容量过小，将导致对象构造的失败。
//        2.初始化一个buffer，该buffer大于等于AudioRecord对象用于写声音数据的buffer大小。
//        3.开始录音
//        4.创建一个数据流，一边从AudioRecord中读取声音数据到初始化的buffer，一边将buffer中数据导入数据流。
//        5.关闭数据流
//        6.停止录音

public class AudioRecordTest extends AppCompatActivity {

    private int minBufferSize;
    private AudioRecord audioRecord;
    private byte[] bytes;
    private boolean isRecording;
    private TextView tv1;
    private View tv2;
    private int sampleRateInHz = 44100;//采样率
    private int channel = 2;//声道个数，
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int chanelConfig = channel ==1 ? AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO;
    private String pcmPath = Environment.getExternalStorageDirectory()+ File.separator+"lh_pcm.pcm";
    private String wavePath = Environment.getExternalStorageDirectory()+ File.separator+"lh_pcm.wav";
    private View tv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.tx1);
        tv2 = findViewById(R.id.tx2);
        tv3 = findViewById(R.id.tx3);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTx();
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTran();
            }
        });
        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        init();
        checkPermissions();
    }
    private AudioTrack audioTrack;
    private void play() {
        new Thread(new Runnable() {


            @Override
            public void run() {
                //声道模式
//                int chanelConfig = channel ==1 ? AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO;
                //声道格式
//                audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRateInHz,chanelConfig,audioFormat,minBufferSize,AudioTrack.MODE_STREAM);
                try {
                    FileInputStream fis = new FileInputStream(pcmPath);
                    audioTrack.play();
                    while(fis.available()>0){
                        int len = fis.read(bytes);
                        if (len == AudioTrack.ERROR_INVALID_OPERATION ||
                                len == AudioTrack.ERROR_BAD_VALUE) {
                            continue;
                        }
                        if(len>0){
                            audioTrack.write(bytes,0,len);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    private void releaseAudioTrack() {
        if (this.audioTrack != null) {
            Log.d("AudioRecordTest", "Stopping");
            audioTrack.stop();
            Log.d("AudioRecordTest", "Releasing");
            audioTrack.release();
            Log.d("AudioRecordTest", "Nulling");
        }
    }

    private void handleTran() {
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(sampleRateInHz,audioFormat,channel);
        pcmToWavUtil.writeWave(pcmPath,wavePath);
    }

    private void handleTx(){
        isRecording = !isRecording;
        if(isRecording){
            tv1.setText("开始录音");
            Log.e("AudioRecordTest","开始录音");
            start();
        }else {
            tv1.setText("停止录音");
            Log.e("AudioRecordTest","停止录音");
            stop();
        }
    }


    //声道格式 、 声道模式
    private void init() {
        //声道模式
//        int chanelConfig = channel ==1 ? AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO;
        //声道格式
//        audioFormat = AudioFormat.ENCODING_PCM_16BIT;

//        1.构造一个AudioRecord对象，其中需要的最小录音缓存buffer大小可以通过getMinBufferSize方法得到。如果buffer容量过小，将导致对象构造的失败。
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, chanelConfig, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, chanelConfig, audioFormat, minBufferSize);
//        2.初始化一个buffer，该buffer大于等于AudioRecord对象用于写声音数据的buffer大小。
        bytes = new byte[minBufferSize];
    }

    private void start() {
        startSave();
    }

    private void stop() {
        audioRecord.stop();
    }

    private void close(){
        stop();
        audioRecord.release();
        audioRecord = null;
    }

    private void startSave() {
        //4.创建一个数据流，一边从AudioRecord中读取声音数据到初始化的buffer，一边将buffer中数据导入数据流。
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(pcmPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if(fos!=null){
                    while (isRecording){
                        audioRecord.startRecording();
                        int read = audioRecord.read(bytes, 0, minBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件 .这里应该是>0 .排除错误，并有数据
//                        if(AudioRecord.ERROR_INVALID_OPERATION !=read)
                        if(read>0){
                            try {
                                fos.write(bytes,0,read);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static final int MY_PERMISSIONS_REQUEST = 1001;
    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("asdf", permissions[i] + " 权限被用户禁止！");
                }
            }
            // 运行时权限的申请不是本demo的重点，所以不再做更多的处理，请同意权限申请。
        }
    }
    /**
     * 被用户拒绝的权限列表
     */
    private List<String> mPermissionList = new ArrayList<>();
    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
//        }
    }

}
