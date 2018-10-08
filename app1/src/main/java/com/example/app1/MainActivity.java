package com.example.app1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.applib1.JniFFmpegConfig;
import com.example.applib1.JniTest;
import com.example.applib1.PermissionManager;
import com.example.applib1.pushstream.LivePusher;
import com.example.applib1.pushstream.listener.LiveStateChangeListener;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends AppCompatActivity implements LiveStateChangeListener{


    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tx = findViewById(R.id.tx_content);
        surfaceView = findViewById(R.id.surfaceView);
//        test1();
//        test3();
//
//        test2();

        test4();

    }

    private void test3() {

        //new SurfaceView(this);
//        surfaceView = findViewById(R.id.surfaceView);
//        SurfaceHolder holder = surfaceView.getHolder();
//        holder.setFormat(PixelFormat.RGBA_8888);

        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                JniTest jniTest = new JniTest();
                Surface surface = surfaceView.getHolder().getSurface();
//                String video = "Forrest_Gump_IMAX.mp4";
//                String video = "中国合伙人.flv";
                String video = "屌丝男士.mov";
                String input = new File(Environment.getExternalStorageDirectory(),video).getAbsolutePath();
//                jniTest.render(input,surface);
                String output = new File(Environment.getExternalStorageDirectory(),"lhtest1.pcm").getAbsolutePath();
//                jniTest.sound(input, output);
                jniTest.play(input, output,surface);
            }
        });
//        ViewGroup viewGroup = findViewById(R.id.rootView);
//        viewGroup.addView(surfaceView,0);

    }

    /* start 权限申请 */
    private PermissionManager mPermissionManager;

    public PermissionManager getPermissionManager() {
        if (mPermissionManager == null) {
            mPermissionManager = new PermissionManager(this);
        }
        return mPermissionManager;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionManager != null) {
            boolean success = mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
            onRequestPermissionsResult(requestCode, permissions, grantResults, success);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, boolean success) {
    }
    /* end 权限申请 */

    private void test2() {
//        JniFFmpegConfig jniFFmpegConfig = new JniFFmpegConfig();
//        jniFFmpegConfig.startTest();
    }

    private void test1() {
        JniTest jniTest = new JniTest();
        try {
            jniTest.startTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    static final String URL = "rtmp://112.74.96.116/live/jason";
    private LivePusher live;
    public void mStartLive(View view) {
        Button btn = (Button)view;
        if(btn.getText().equals("开始直播")){
            live.startPush(URL,this);
            btn.setText("停止直播");
        }else{
            live.stopPush();
            btn.setText("开始直播");
        }
    }

    public void mSwitchCamera(View view) {
        live.switchCamera();
    }

    private void test4() {
        getPermissionManager().requestPermissions(
                Manifest.permission.RECORD_AUDIO);
        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                //相机图像的预览
                live = new LivePusher(surfaceView.getHolder());
//                JniTest jniTest = new JniTest();
//                Surface surface = surfaceView.getHolder().getSurface();
////                String video = "Forrest_Gump_IMAX.mp4";
////                String video = "中国合伙人.flv";
//                String video = "屌丝男士.mov";
//                String input = new File(Environment.getExternalStorageDirectory(),video).getAbsolutePath();
////                jniTest.render(input,surface);
//                String output = new File(Environment.getExternalStorageDirectory(),"lhtest1.pcm").getAbsolutePath();
////                jniTest.sound(input, output);
//                jniTest.play(input, output,surface);
            }
        });
    }

    @Override
    public void onError(int code) {

    }
}
