package com.example.av_sample.drawbitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.example.av_sample.R;

import java.io.File;

public class DrawBitmapActivity extends AppCompatActivity {

    private ImageView img;
    private SurfaceView sv;
    private CustomView cv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_bitmap);
        img = findViewById(R.id.img);
        sv = findViewById(R.id.sv);
        cv = findViewById(R.id.cv);
        draw();
    }

    private void draw() {
        draw1();
        draw2();
        draw3();
    }

    private void draw1() {
        //ImageView渲染
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "1.png");
        img.setImageBitmap(bitmap);
    }

    private void draw2() {
        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(holder == null){
                    return;
                }
                //画笔：
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);

                Canvas canvas = holder.lockCanvas();
                Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator + "1.png");
                canvas.drawBitmap(bitmap,0,0,paint);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
    private void draw3() {

    }

}
