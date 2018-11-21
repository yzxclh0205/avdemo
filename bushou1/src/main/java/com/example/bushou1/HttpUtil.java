package com.example.bushou1;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.TextureView;
import android.widget.Toast;

import org.json.JSONArray;

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
 *
 * @author robinzhang
 */
public class HttpUtil {
    /**
     * 请求类型： GET
     */
    public final String GET = "GET";
    /**
     * 请求类型： POST
     */
    public final String POST = "POST";
    public final int NO_COUPON_ID = 1;
    public final int NO_COOKIE = 2;
    public final int NO_URL = 3;
    public boolean isOwn;

    public boolean isOwn() {
        return isOwn;
    }

    public void setOwn(boolean own) {
        isOwn = own;
    }

    /**
     * 模拟Http Get请求
     *
     * @param urlStr   请求路径
     * @param paramMap 请求参数
     * @return
     * @throws Exception
     */
    public String get(String urlStr, Map<String, String> paramMap) throws Exception {
        if (paramMap != null) {
            urlStr = urlStr + "?" + getParamString(paramMap);
        }
        HttpURLConnection conn = null;
        try {
            //创建URL对象
            URL url = new URL(urlStr);
            //获取URL连接
            conn = (HttpURLConnection) url.openConnection();
            //设置通用的请求属性
            try {
                setHttpUrlConnection(conn, GET);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            //设置通用请求属性
            ddd.trustAllHosts((HttpsURLConnection) conn);
            ((HttpsURLConnection) conn).getHostnameVerifier();
            ((HttpsURLConnection) conn).setHostnameVerifier(ddd.DO_NOT_VERIFY);
            //建立实际的连接
            conn.connect();
            //获取响应的内容
            return readResponseContent(conn.getInputStream());
        } finally {
            if (null != conn) conn.disconnect();
        }
    }

    /**
     * 模拟Http Post请求
     *
     * @param urlStr   请求路径
     * @param paramMap 请求参数
     * @return
     * @throws Exception
     */
    public String post(String urlStr, Map<String, String> paramMap) throws Exception {
        HttpURLConnection conn = null;
        PrintWriter writer = null;
        try {
            //创建URL对象
            URL url = new URL(urlStr);
            //获取请求参数
            String param = getParamString(paramMap);
            //获取URL连接
            conn = (HttpURLConnection) url.openConnection();
            //设置通用请求属性
            try {
                setHttpUrlConnection(conn, POST);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            ddd.trustAllHosts((HttpsURLConnection) conn);
            ((HttpsURLConnection) conn).getHostnameVerifier();
            ((HttpsURLConnection) conn).setHostnameVerifier(ddd.DO_NOT_VERIFY);
            //建立实际的连接
            conn.connect();
            //将请求参数写入请求字符流中
            writer = new PrintWriter(conn.getOutputStream());
            writer.print(param);
            writer.flush();
            //读取响应的内容
            return readResponseContent(conn.getInputStream());
        } finally {
            if (null != conn) conn.disconnect();
            if (null != writer) writer.close();
        }
    }

    /**
     * 读取响应字节流并将之转为字符串
     *
     * @param in 要读取的字节流
     * @return
     * @throws IOException
     */
    private String readResponseContent(InputStream in) throws IOException {
        Reader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            reader = new InputStreamReader(in);
            char[] buffer = new char[1024];
            int head = 0;
            while ((head = reader.read(buffer)) > 0) {
                content.append(new String(buffer, 0, head));
            }
            return content.toString();
        } finally {
            if (null != in) in.close();
            if (null != reader) reader.close();
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

//    https://m.51bushou.com/ygg-hqbs/coupon/doCmsCoupon?cId=5795&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.73&sign=A1EB471688FF6B8E&channel=5
//    Host: m.51bushou.com
//    Connection: keep-alive
//    Content-Length: 16
//    Accept: */*
//Origin: https://m.51bushou.com
//X-Requested-With: XMLHttpRequest
//User-Agent: Mozilla/5.0 (Linux; Android 8.0.0; MI 5 Build/OPR1.170623.032; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/62.0.3202.84 Mobile Safari/537.36/Android/globalscanner/3.73
//Content-Type: application/x-www-form-urlencoded
//Referer: https://m.51bushou.com/cms/index.html?cId=5795&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.73&sign=A1EB471688FF6B8E&channel=5
//Accept-Encoding: gzip, deflate
//Accept-Language: zh-CN,en-US;q=0.9
//Cookie: JSESSIONID=D98F5071641B662C5ADA8CD7B2A00DA0; aliyungf_tc=AQAAALGpuUacSAoA1VtXcbeJ60jBPzk6; QQBS382362133=b879623f47; QQBSLG409292508=1; SERVERID=5d458c4f769d4f755a7355111edd6a90|1542769634|1542766242


//    https://m.51bushou.com/cms/index.html?cId=5795&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.73&sign=A1EB471688FF6B8E&channel=5
//    accept: */*
//Connection: Keep-Alive
//Content-Length: 16
//Accept-Language: zh-CN,en-US;q=0.8
//Content-Type: application/x-www-form-urlencoded
//Accept-Encoding: gzip, deflate
//Cookie: JSESSIONID=D98F5071641B662C5ADA8CD7B2A00DA0; aliyungf_tc=AQAAALGpuUacSAoA1VtXcbeJ60jBPzk6; QQBS382362133=b879623f47; QQBSLG409292508=1; SERVERID=5d458c4f769d4f755a7355111edd6a90|1542769634|1542766242
//User-Agent: Mozilla/5.0 (Linux; Android 8.0.0; MI 5 Build/OPR1.170623.032; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/62.0.3202.84 Mobile Safari/537.36/Android/globalscanner/3.73
//Host: m.51bushou.com

    private void setHttpUrlConnection(HttpURLConnection conn, String requestMethod) throws ProtocolException {
        conn.setRequestMethod(requestMethod);
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Length", "16");
//        conn.setRequestProperty("X-Requested-With", "16");
        conn.setRequestProperty("Accept-Language", "zh-CN,en-US;q=0.8");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        if (TextUtils.isEmpty(getCookie())) {
            handler.sendEmptyMessage(NO_COOKIE);
            throw new NullPointerException("no cookie");
        }
        conn.setRequestProperty("Cookie", getCookie());
        if (null != requestMethod && POST.equals(requestMethod))
//        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; ALE-UL00 Build/HuaweiALE-UL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.105 Mobile Safari/537.36/Android/globalscanner/3.80");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; MI 5 Build/OPR1.170623.032; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/62.0.3202.84 Mobile Safari/537.36/Android/globalscanner/3.73");
//        conn.setRequestProperty("Proxy-Connection", "Keep-Alive");
        if (null != requestMethod && POST.equals(requestMethod)) {
            conn.setDoOutput(true);
            conn.setDoInput(true);
        }
    }

    /**
     * 将参数转为路径字符串
     * 参数
     *
     * @return
     */
    private String getParamString(Map<String, String> paramMap) {
        if (null == paramMap || paramMap.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String key : paramMap.keySet()) {
            builder.append("&")
                    .append(key).append("=").append(paramMap.get(key));
        }
        return builder.deleteCharAt(0).toString();
    }

    private boolean isExit;

    public void setExit(boolean exit) {
        isExit = exit;
    }

//    private String cookie;

//    String cookie = (String) SPUtils.get(this, "cookie", "");
//    String couponid = (String) SPUtils.get(this, "couponid", "");
//    String url = (String) SPUtils.get(this, "url", "");
    public String getCookie() {
        if (isOwn()) {
            return "JSESSIONID=D98F5071641B662C5ADA8CD7B2A00DA0; aliyungf_tc=AQAAALGpuUacSAoA1VtXcbeJ60jBPzk6; QQBS382362133=b879623f47; QQBSLG409292508=1; SERVERID=5d458c4f769d4f755a7355111edd6a90|1542769634|1542766242";
        } else {
            return (String) SPUtils.get(context, "cookie", "");
        }
//        return "JSESSIONID=D73AC6EF5BE0C9737EC8008E13DF03AE; aliyungf_tc=AQAAALGpuUacSAoA1VtXcbeJ60jBPzk6; QQBS382362133=b879623f47; QQBSLG409292508=1; SERVERID=5d458c4f769d4f755a7355111edd6a90|1542767152|1542766242";
//        return cookie;//"JSESSIONID=27DCDE32FB6E52EFEE836BDC393251E2; aliyungf_tc=AQAAAOYHlChUQAcAapx0cVqPeRO4YvqQ; QQBS382362133=fe663c4943; QQBSLG409292508=1; SERVERID=7479bab27a19c348720ed00921b092ff|1541642840|1541640896";
    }

    public void setCookie(String cookie) {
//        this.cookie = cookie;
        SPUtils.put(context,"cookie",cookie);
    }

//    private String couponId;

    public String getCouponId() {
        String couponid = (String) SPUtils.get(context, "couponid", "");
        System.out.println("getCouponId "+couponid);
        return couponid;
    }

    public void setCouponId(String couponId) {
//        this.couponId = couponId;
        SPUtils.put(context,"couponid",couponId);
        String couponid = (String) SPUtils.get(context, "couponid", "");
        System.out.println("setCouponId "+couponid);
    }

//    private String url = null;

    public String getUrl() {
        if (isOwn()) {
            return "https://m.51bushou.com/ygg-hqbs/coupon/doCmsCoupon?cId=5795&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.73&sign=A1EB471688FF6B8E&channel=5";
        } else {
            return (String) SPUtils.get(context, "url", "");
        }
        //        return "https://m.51bushou.com/cms/index.html?cId=5795&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.73&sign=A1EB471688FF6B8E&channel=5";
//        return url;//"https://m.51bushou.com/ygg-hqbs/coupon/doCmsCoupon?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3";
    }

    public void setUrl(String url) {
//        this.url = url;
        SPUtils.put(context,"url",url);
    }

    //    https://m.51bushou.com/ygg-hqbs/coupon/doCmsCoupon?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3
    Map<String, String> map = null;
    private Context context;
    Handler handler;

    public HttpUtil(Context context) {
        this.context = context;
    }

    public void main(final Context context, final Handler handler1) {
        this.context = context;
        this.handler = handler1;
        map = new HashMap<>();
        map.put("cmsCouponId", getCouponId());
        try {
//            System.out.println( get("http://127.0.0.1/crazy_java.pdf", null) );

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        for(int i=0;i<1;i++){
                        int i = 0;
                        while (true) {
                            if (isExit) {
                                break;
                            }
                            if (TextUtils.isEmpty(getCouponId()) ||
                                    (!getCouponId().startsWith("1") || getCouponId().length() < 3)
                                    ) {
                                if(handler1!=null)
                                handler.sendEmptyMessage(NO_COUPON_ID);
                                break;
                            }
                            if (TextUtils.isEmpty(getCookie()) ||
                                    (getCookie().length() < 10)
                                    ) {
                                if(handler1!=null)
                                handler.sendEmptyMessage(NO_COOKIE);
                                break;
                            }
                            if (TextUtils.isEmpty(getUrl()) || getUrl().length() < 10) {
                                if(handler1!=null)
                                handler.sendEmptyMessage(NO_URL);
                                break;
                            }
                            i++;
                            String post = post(getUrl(), map);
                            String a = "\"status\":1";
                            String a5 = "\"status\":5";
                            String a1 = "\"status\":3";
                            String a2 = "\"status\":\"1\"";
                            String a3 = "\"status\":\"3\"";
                            String a51 = "\"status\":\"5\"";
                            System.out.println("asdf   " + post);
                            Message msg = Message.obtain();
                            msg.obj = "第 " + i + " 次请求结果如下：\n" + post;
                            if(handler1!=null)
                            handler.sendMessage(msg);
                            if (!TextUtils.isEmpty(post) && (
                                    post.replaceAll(" ", "").contains(a) || post.replaceAll(" ", "").contains(a1)
                                            || post.replaceAll(" ", "").contains(a2) || post.replaceAll(" ", "").contains(a3)
                                            || post.replaceAll(" ", "").contains(a5) || post.replaceAll(" ", "").contains(a51)
                            )) {
                                Notification notification = new NotificationUtils(context).sendNotification("结果", post);
                                new NotificationUtils(context).getManager().notify(1,notification);
                                showToast();
                                break;
                            }
//                            Thread.sleep(90);
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

//    Host: m.51bushou.com
//    Connection: keep-alive
//    Accept: application/json
//    X-Requested-With: XMLHttpRequest
//    User-Agent: Mozilla/5.0 (Linux; Android 6.0; ALE-UL00 Build/HuaweiALE-UL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.105 Mobile Safari/537.36/Android/globalscanner/3.80/Android/globalscanner/3.80/Android/globalscanner/3.80/Android/globalscanner/3.80
//    Referer: https://m.51bushou.com/cms/index.html?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3
//    Accept-Encoding: gzip, deflate
//    Accept-Language: zh-CN,en-US;q=0.8
//    Cookie: JSESSIONID=27DCDE32FB6E52EFEE836BDC393251E2; aliyungf_tc=AQAAAOYHlChUQAcAapx0cVqPeRO4YvqQ; QQBS382362133=fe663c4943; QQBSLG409292508=1; SERVERID=7479bab27a19c348720ed00921b092ff|1541642840|1541640896

//    Host: m.51bushou.com
//    Connection: keep-alive
//    Accept: application/json
//    X-Requested-With: XMLHttpRequest
//    User-Agent: Mozilla/5.0 (Linux; Android 6.0; ALE-UL00 Build/HuaweiALE-UL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.105 Mobile Safari/537.36/Android/globalscanner/3.80/Android/globalscanner/3.80/Android/globalscanner/3.80/Android/globalscanner/3.80User-Agent: Mozilla/5.0 (Linux; Android 6.0; ALE-UL00 Build/HuaweiALE-UL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.105 Mobile Safari/537.36/Android/globalscanner/3.80/Android/globalscanner/3.80/Android/globalscanner/3.80/Android/globalscanner/3.80
//    Referer: https://m.51bushou.com/cms/index.html?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3
//    Accept-Encoding: gzip, deflate
//    Accept-Language: zh-CN,en-US;q=0.8
//    Cookie: JSESSIONID=27DCDE32FB6E52EFEE836BDC393251E2; aliyungf_tc=AQAAAOYHlChUQAcAapx0cVqPeRO4YvqQ; QQBS382362133=fe663c4943; QQBSLG409292508=1; SERVERID=7479bab27a19c348720ed00921b092ff|1541642840|1541640896


    public void getFirstAvaiableCouponId() {
        final String url = "https://m.51bushou.com/ygg-hqbs/coupon/doCmsCoupon?cId=5618&APPOS=1&isApp=1&os=2&accountId=1286141704&version=3.80&sign=A1EB471688FF6B8E&channel=3";
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String s = get(url, null);
                        System.out.println(s);
//                        JSONArray componentList = JSON.
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast() {
        if(context!=null & context instanceof Activity){

            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
//                    ((Activity)context).setText();
                    Toast.makeText(context, "结束", Toast.LENGTH_LONG).show();
                }

            });
        }
    }
}