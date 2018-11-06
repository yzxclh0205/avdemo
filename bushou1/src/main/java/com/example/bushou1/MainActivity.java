package com.example.bushou1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView tx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         tx = findViewById(R.id.tx);
         tx.setOnClickListener(this);
    }

    public void setText() {
        tx.setText("抢到了，去看看");
    }

    @Override
    public void onClick(View v) {
        if("点击开始".equals(tx.getText().toString().trim())){
            new HttpUtil().main(this);
            tx.setText("执行中...");
        }
    }
}
