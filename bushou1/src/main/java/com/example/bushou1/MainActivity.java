package com.example.bushou1;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Handler handler;
    TextView tx;
    TextView tx_contenet;
    HttpUtil httpUtil;
    private EditText et_couponid;
    private EditText et_cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        tx = findViewById(R.id.tx);
        tx_contenet = findViewById(R.id.tx_contenet);
        tx.setOnClickListener(this);
        httpUtil = new HttpUtil();
    }

    private void handleWatcher() {
        et_couponid = findViewById(R.id.et_couponid);
        et_couponid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setCouponId();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_cookie = findViewById(R.id.et_cookie);
        et_cookie.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setCookie();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        initParams();
;
    }

    private void initParams() {
        String cookie = (String) SPUtils.get(this, "cookie", "");
        String couponid = (String) SPUtils.get(this, "couponid", "");
        et_cookie.setText(cookie);
        et_couponid.setText(couponid);
        httpUtil.setCookie(cookie);
        httpUtil.setCouponId(couponid);
    }

    private void setCookie(){
        String trim = et_cookie.getText().toString().trim();
        SPUtils.put(this,"cookie",trim);
        httpUtil.setCookie(trim);
    }
    private void setCouponId(){
        String trim = et_couponid.getText().toString().trim();
        SPUtils.put(this,"couponid",trim);
        httpUtil.setCouponId(trim);
    }

    private boolean hasReq;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasReq){
            handleWatcher();
            httpUtil.getFirstAvaiableCouponId();
            hasReq = true;
        }
    }

    private void init() {
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg!=null){
                    if(msg.what ==httpUtil.NO_COOKIE){
                        tx_contenet.setText("cookie再检查检查");
                        Toast.makeText(MainActivity.this,"cookie再检查检查",Toast.LENGTH_SHORT).show();
                        stop();
                    }else if(msg.what == httpUtil.NO_COUPON_ID){
                        tx_contenet.setText("优惠券id再检查检查");
                        Toast.makeText(MainActivity.this,"优惠券id再检查检查",Toast.LENGTH_SHORT).show();
                        stop();
                    }
                    else {
                        if (msg.obj != null && tx != null) {
                            tx_contenet.setText(msg.obj.toString());
                        }
                    }
                }

            }
        };
    }

    public void setText() {
        tx.setText("抢到了，去看看");
    }

    @Override
    public void onClick(View v) {
        click();
    }

    private void click(){
        if ("点击开始".equals(tx.getText().toString().trim())) {
            httpUtil.setExit(false);
            httpUtil.main(this, handler);
            tx.setText("执行中... 再次点击结束");
        } else {
            httpUtil.setExit(true);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tx.setText("点击开始");
        }
    }

    private void stop(){
        httpUtil.setExit(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tx.setText("点击开始");
    }
}
