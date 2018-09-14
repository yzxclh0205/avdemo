package com.example.app1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.applib1.JniFFmpegConfig;
import com.example.applib1.JniTest;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tx = findViewById(R.id.tx_content);
        test1();
        test2();

    }

    private void test2() {
//        JniFFmpegConfig jniFFmpegConfig = new JniFFmpegConfig();
//        jniFFmpegConfig.startTest();
    }

    private void test1() {
        JniTest jniTest = new JniTest();
        try {
            jniTest.startTest();
        } catch (UnsupportedEncodingException e) {
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

}
