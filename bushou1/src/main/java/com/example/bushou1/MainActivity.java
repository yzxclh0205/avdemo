package com.example.bushou1;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    private TextView tx;
    private TextView tx_contenet;
    private HttpUtil httpUtil;
    private EditText et_couponid;
    private EditText et_cookie;
    private EditText et_url;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            TestService.MyBinder myBinder = (TestService.MyBinder)binder;
//            service = myBinder.getService();
//            Log.i("DemoLog", "ActivityA onServiceConnected");
//            int num = service.getRandomNumber();
//            Log.i("DemoLog", "ActivityA 中调用 TestService的getRandomNumber方法, 结果: " + num)！
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        tx = findViewById(R.id.tx);
        tx_contenet = findViewById(R.id.tx_contenet);
        tx.setOnClickListener(this);
        httpUtil = new HttpUtil(this);
        tx_contenet.setOnClickListener(this);

//        bindServiceT();
        startServiceT();
    }

    private void startServiceT() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(new Intent(this, LongRunningService.class));
        } else {
            this.startService(new Intent(this, LongRunningService.class));
        }
    }

    private void bindServiceT() {
        Intent intent = new Intent(this,CustomTestService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }

    /**
     * 实现双击方法
     * src 拷贝的源数组
     * srcPos 从源数组的那个位置开始拷贝.
     * dst 目标数组
     * dstPos 从目标数组的那个位子开始写数据
     * length 拷贝的元素的个数
     */
    final static int COUNTS = 5;//点击次数
    final static long DURATION = 3 * 1000;//规定有效时间
    long[] mHits = new long[COUNTS];
    private void continuityClick(){
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
            httpUtil.setOwn(true);
            String tip = "own set";
//            String tips = "您已在[" + DURATION + "]ms内连续点击【" + mHits.length + "】次了！！！";
            Toast.makeText(MainActivity.this, tip, Toast.LENGTH_SHORT).show();
        }
    }


    private void handleWatcher() {
        et_url = findViewById(R.id.et_url);
        et_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setUrl();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
    }

    private void setUrl() {
        String trim = et_url.getText().toString().trim();
        httpUtil.setUrl(trim);
    }

    private void initParams() {
        String cookie = (String) SPUtils.get(this, "cookie", "");
        String couponid = (String) SPUtils.get(this, "couponid", "");
        String url = (String) SPUtils.get(this, "url", "");
        et_url.setText(url);
        et_cookie.setText(cookie);
        et_couponid.setText(couponid);
        httpUtil.setUrl(url);
        httpUtil.setCookie(cookie);
        httpUtil.setCouponId(couponid);
    }

    private void setCookie(){
        String trim = et_cookie.getText().toString().trim();
        httpUtil.setCookie(trim);
    }
    private void setCouponId(){
        String trim = et_couponid.getText().toString().trim();
        httpUtil.setCouponId(trim);
    }

    private boolean hasReq;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasReq){
            handleWatcher();
//            httpUtil.getFirstAvaiableCouponId();
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
                    }else if(msg.what == httpUtil.NO_URL){
                        tx_contenet.setText("URL再检查检查");
                        Toast.makeText(MainActivity.this,"URL再检查检查",Toast.LENGTH_SHORT).show();
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
        if(v.getId() == R.id.tx_contenet){
            continuityClick();
        }else if(v.getId() == R.id.tx){
            click();
        }
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
