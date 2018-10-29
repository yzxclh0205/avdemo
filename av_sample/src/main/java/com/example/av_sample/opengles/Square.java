package com.example.av_sample.opengles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by lh on 2018/10/29.
 */

//定义形状、 绘制形状
//定义形状、 绘制形状
public class Square {

    //正方形四个点 坐标
    private static float squareCoords[] = {
            -0.5f,0.5f,0f,
            -0.5f,-0.5f,0f,
            0.5f,-0.5f,0f,
            0.5f,0.5f,0f
    };

    //点的绘制顺序
    private short drawOrder[] = {0,1,2,0,2,3};

    public Square() {
        //给图形初始化顶点
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(squareCoords);
        floatBuffer.position(0);

        //给绘制列表点 初始化绘制缓存
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = dlb.asShortBuffer();
        shortBuffer.put(drawOrder);
        shortBuffer.position(0);

    }
}
