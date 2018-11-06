package com.example.bushou1;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.TextureView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * HTTP工具
 * @author robinzhang
 *
 */
public class HttpUtil {
    /**
     * 请求类型： GET
     */
    public final static String GET = "GET";
    /**
     * 请求类型： POST
     */
    public final static String POST = "POST";
    
    /**
     * 模拟Http Get请求
     * @param urlStr
     *             请求路径
     * @param paramMap
     *             请求参数
     * @return
     * @throws Exception
     */
    public static String get(String urlStr, Map<String, String> paramMap) throws Exception{
        urlStr = urlStr + "?" + getParamString(paramMap);
        HttpURLConnection conn = null;
        try{
            //创建URL对象
            URL url = new URL(urlStr);
            //获取URL连接
            conn = (HttpURLConnection) url.openConnection();
            //设置通用的请求属性
            setHttpUrlConnection(conn, GET);
            //建立实际的连接
            conn.connect();
            //获取响应的内容
            return readResponseContent(conn.getInputStream());
        }finally{
            if(null!=conn) conn.disconnect();
        }
    }
    
    /**
     * 模拟Http Post请求
     * @param urlStr
     *             请求路径
     * @param paramMap
     *             请求参数
     * @return
     * @throws Exception 
     */
    public static String post(String urlStr, Map<String, String> paramMap) throws Exception{
        HttpURLConnection conn = null;
        PrintWriter writer = null;
        try{
            //创建URL对象
            URL url = new URL(urlStr);
            //获取请求参数
            String param = getParamString(paramMap);
            //获取URL连接
            conn = (HttpURLConnection) url.openConnection();
            //设置通用请求属性
            setHttpUrlConnection(conn, POST);
            ddd.trustAllHosts((HttpsURLConnection)conn);
            ((HttpsURLConnection)conn).getHostnameVerifier();
            ((HttpsURLConnection)conn).setHostnameVerifier(ddd.DO_NOT_VERIFY);
            //建立实际的连接
            conn.connect();
            //将请求参数写入请求字符流中
            writer = new PrintWriter(conn.getOutputStream());
            writer.print(param);
            writer.flush();
            //读取响应的内容
            return readResponseContent(conn.getInputStream());
        }finally{
            if(null!=conn) conn.disconnect();
            if(null!=writer) writer.close();
        }
    }
    
    /**
     * 读取响应字节流并将之转为字符串
     * @param in
     *         要读取的字节流
     * @return
     * @throws IOException
     */
    private static String readResponseContent(InputStream in) throws IOException{
        Reader reader = null;
        StringBuilder content = new StringBuilder();
        try{
            reader = new InputStreamReader(in);
            char[] buffer = new char[1024];
            int head = 0;
            while( (head=reader.read(buffer))>0 ){
                content.append(new String(buffer, 0, head));
            }
            return content.toString();
        }finally{
            if(null!=in) in.close();
            if(null!=reader) reader.close();
        }
    }


//    POST https://m.51bushou.com/ygg-hqbs/coupon/doCmsCoupon?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3 HTTP/1.1
//    Host: m.51bushou.com
//    Connection: keep-alive
//    Content-Length: 16
//    Accept: */*
//Origin: https://m.51bushou.com
//X-Requested-With: XMLHttpRequest
//User-Agent: Mozilla/5.0 (Linux; Android 6.0; ALE-UL00 Build/HuaweiALE-UL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.105 Mobile Safari/537.36/Android/globalscanner/3.80
//Content-Type: application/x-www-form-urlencoded
//Referer: https://m.51bushou.com/cms/index.html?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3
//Accept-Encoding: gzip, deflate
//Accept-Language: zh-CN,en-US;q=0.8
//Cookie: JSESSIONID=3C9829DC542698627DE224295538539B; aliyungf_tc=AQAAAOYHlChUQAcAapx0cVqPeRO4YvqQ; QQBS382362133=fe663c4943; QQBSLG409292508=1; SERVERID=fbd9119b957a01fe4690200d4104c104|1541492426|1541486950
//
//cmsCouponId=1179
    private static void setHttpUrlConnection(HttpURLConnection conn, String requestMethod) throws ProtocolException{
        conn.setRequestMethod(requestMethod);
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Length", "16");
        conn.setRequestProperty("X-Requested-With", "16");
        conn.setRequestProperty("Accept-Language", "zh-CN,en-US;q=0.8");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Cookie", "JSESSIONID=3C9829DC542698627DE224295538539B; aliyungf_tc=AQAAAOYHlChUQAcAapx0cVqPeRO4YvqQ; QQBS382362133=fe663c4943; QQBSLG409292508=1; SERVERID=fbd9119b957a01fe4690200d4104c104|1541492426|1541486950");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; ALE-UL00 Build/HuaweiALE-UL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.105 Mobile Safari/537.36/Android/globalscanner/3.80");
//        conn.setRequestProperty("Proxy-Connection", "Keep-Alive");
        if(null!=requestMethod && POST.equals(requestMethod)){
            conn.setDoOutput(true);
            conn.setDoInput(true);
        }
    }
    
    /**
     * 将参数转为路径字符串
     *             参数
     * @return
     */
    private static String getParamString(Map<String, String> paramMap){
        if(null==paramMap || paramMap.isEmpty()){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(String key : paramMap.keySet() ){
            builder.append("&")
                   .append(key).append("=").append(paramMap.get(key));
        }
        return builder.deleteCharAt(0).toString();
    }
    String url = null;
    Map<String,String> map = null;
    private MainActivity context;
    public void main(MainActivity context){
        this.context = context;
        url= "https://m.51bushou.com/ygg-hqbs/coupon/doCmsCoupon?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3";
        map = new HashMap<>();
        map.put("cmsCouponId","1179");
        try {
//            System.out.println( get("http://127.0.0.1/crazy_java.pdf", null) );

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        for(int i=0;i<1;i++){
                        while (true){
                            String post = post(url, map);
                            String a = "\"status\":1";
                            String a1 = "\"status\":\"1\"";
                            System.out.println("asdf   "+post);
                            if(!TextUtils.isEmpty(post)&&(post.replaceAll(" ","").contains(a) || post.replaceAll(" ","").contains(a1))){
                                showToast();
                                break;
                            }
                            Thread.sleep(90);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(){

        context.runOnUiThread(new Runnable()
        {
            public void run()
            {
                context.setText();
                Toast.makeText(context , "抢到", Toast.LENGTH_LONG).show();
            }

        });
    }
}