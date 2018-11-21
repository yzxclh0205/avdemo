package com.example.bushou1;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LongRunningService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("LongRunningService", "executed at " + getCurrTime());
                long l = System.currentTimeMillis();
                if(l > getDelay2() && l <getDelay3()){
                    Log.d("LongRunningService", "开始执行程序 " + getCurrTime());
                    HttpUtil httpUtil = new HttpUtil(LongRunningService.this);
                    httpUtil.setOwn(true);
                    httpUtil.main(LongRunningService.this,null);
                }

            }
        }).start();

        startForeground(1234, new Notification());
        stopForeground(true);
//        showNotify();
        String str = getCurrTime() + "捕手 ";
        Notification notification = new NotificationUtils(this).sendNotification(str, str);
        startForeground(1234, notification);
//        showN();

        long time = getDelay1();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long delay =  time - System.currentTimeMillis();//System.10 * 1000; // 每十秒
        long triggerAtTime = SystemClock.elapsedRealtime() + 9 * 1000;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private int index = 0;

    private long getDelay1() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 16);
        c.set(Calendar.MINUTE, 18 + index);
        c.set(Calendar.SECOND, 0);
        long time = c.getTime().getTime();
        index++;
        return time;
    }

//    private long getDelay2() {
//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.HOUR_OF_DAY, 17);
//        c.set(Calendar.MINUTE, 57);
//        c.set(Calendar.SECOND, 47);
//        long time = c.getTime().getTime();
//        return time;
//    }
//    private long getDelay3() {
//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.HOUR_OF_DAY, 17);
//        c.set(Calendar.MINUTE, 57);
//        c.set(Calendar.SECOND, 58);
//        long time = c.getTime().getTime();
//        return time;
//    }

    private long getDelay2() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 19);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 47);
        long time = c.getTime().getTime();
        return time;
    }
    private long getDelay3() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 19);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 57);
        long time = c.getTime().getTime();
        return time;
    }

    public static String getCurrTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    // 通知栏实现源码
    private void showNotification(Context context, String msg) {
        //        这里只是修改了MyService中onCreate()方法的代码。可以看到，我们首先创建了一个Notification对象，
//        然后调用了它的setLatestEventInfo()方法来为通知初始化布局和数据，
//        并在这里设置了点击通知后就打开MainActivity。然后调用startForeground()方法就可以让MyService变成一个前台Service，并会将通知的图片显示出来。
//        Notification notification = new Notification(R.mipmap.ic_launcher,
//                "bushou有通知到来", System.currentTimeMillis());
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        notification.(this, "bushou通知的标题", "bushou通知的内容", pendingIntent);
//
//        startForeground(1, notification);


        // 点击通知时转移内容
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("state", "1");// 设置标志位
        intent.addCategory(context.WINDOW_SERVICE);
        // 主要是设置点击通知时显示内容的类
        PendingIntent m_PendingIntent = PendingIntent.getActivity(context, 0, intent, 0); // 如果转移内容则用m_Intent();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //构造Notification对象
        Notification.Builder notifyBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)   // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap
                .setTicker(msg)    // 设置在status
                // bar上显示的提示文字
                .setContentTitle(msg)
                .setContentText(getString(R.string.app_name))                // 设置在下拉status
                // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                .setContentIntent(m_PendingIntent);
//        //震动
//        if (ConfigInfo.getBoolShack(getApplicationContext())) {
//            getShock();
////            notifyBuilder.setVibrate(new long[]{100, 250, 100, 500});
//        }
        // 关联PendingIntent
        Notification notify = notifyBuilder.getNotification();
//        notify.flags |= Notification.FLAG_AUTO_CANCEL;
//        if (ConfigInfo.getBoolBellShack(getApplicationContext())) {
//            notify.defaults |= Notification.DEFAULT_SOUND;
//        }
//        if (opened) {
//            builder.notificationDefaults = Notification.DEFAULT_SOUND;  //设置为铃声（ Notification.DEFAULT_SOUND）或者震动（ Notification.DEFAULT_VIBRATE）
//        }
        // 16及之后增加的，在API11中可以使用getNotificatin()来代替
        //声音
        //通过消息类型去通知
//        nm.notify(0, notify);
        startForeground(1, notify);
    }

    private void showNotify() {
        try {
            String str = getCurrTime() + "捕手 ";
            //利用 Notification 类设置通知的属性
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("事件提醒" + str)
                    .setContentText("有事件" + str)
                    //不设置小图标通知栏显示通知（不确定）
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            notification.flags = Notification.FLAG_INSISTENT;
            //利用 NotificationManager 类发出通知栏通知
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(1, notification);

            System.out.println("哈哈");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showN() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String currTime = getCurrTime();
            String str = currTime + "捕手 ";
            String channelId = currTime+"chat";
            String channelName = str;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
//            sendChatMsg();

//            channelId = "subscribe";
//            channelName = "订阅消息";
//            importance = NotificationManager.IMPORTANCE_DEFAULT;
//            createNotificationChannel(channelId, channelName, importance);
        }else {
            showNotification(this,getCurrTime()+"捕手 ");
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private NotificationManager createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        sendChatMsg(notificationManager);
        return notificationManager;
    }

    public void sendChatMsg(NotificationManager notificationManager) {
        NotificationManager manager = notificationManager!=null?notificationManager:(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "chat")
                .setContentTitle("收到一条聊天消息")
                .setContentText("今天中午吃什么？")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .build();
        manager.notify(1, notification);
        System.out.println("哈哈2");
    }
}
