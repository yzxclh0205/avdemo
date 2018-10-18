package com.example.av_sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.av_sample.audiorecord.AudioRecordTest;
import com.example.av_sample.drawbitmap.DrawBitmapActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test1();
//        startActivity(new Intent(this, DrawBitmapActivity.class));
//        startActivity(new Intent(this, AudioRecordTest.class));
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

    private void test1() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.RECORD_AUDIO};
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
