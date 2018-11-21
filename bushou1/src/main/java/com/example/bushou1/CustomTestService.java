package com.example.bushou1;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class CustomTestService extends Service {

    public static final int NOTIFICATION_ID = 1234;

    private static final String TAG = "CustomTestService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (Build.VERSION.SDK_INT < 18) {
            //18以前空通知栏即可
            startForeground(NOTIFICATION_ID, new Notification());
        } else {
            Intent innerIntent = new Intent(this, CustomTestInnerService.class);
            startService(innerIntent);
            startForeground(NOTIFICATION_ID, new Notification());
        }

//        Notification notification = new Notification(R.drawable.ic_launcher,
//                "有通知到来", System.currentTimeMillis());
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);
//        notification.setLatestEventInfo(this, "这是通知的标题", "这是通知的内容",
//                pendingIntent);
//        startForeground(1, notification);
//        这里只是修改了MyService中onCreate()方法的代码。可以看到，我们首先创建了一个Notification对象，
//        然后调用了它的setLatestEventInfo()方法来为通知初始化布局和数据，
//        并在这里设置了点击通知后就打开MainActivity。然后调用startForeground()方法就可以让MyService变成一个前台Service，并会将通知的图片显示出来。
        return super.onStartCommand(intent, flags, startId);
    }

    public static class CustomTestInnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(NOTIFICATION_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }

//    private void start(){
//        Intent intent = new Intent(AlarmTest.this, AlarmActivity.class);
////AlarmActivity就是当闹钟提醒的时候打开的activity,你也可以发送广播
//        intent.setAction("nzy");
//// 创建PendingIntent对象
//        PendingIntent pi = PendingIntent.getActivity(
//                AlarmTest.this, 0, intent, 0);
//        Calendar calendar = Calendar.getInstance();
//// 根据用户选择时间来设置Calendar对象
//        calendar.set(Calendar.HOUR, hourOfDay);
//        calendar.set(Calendar.MINUTE, minute);
//// 设置AlarmManager将在Calendar对应的时间启动指定组件
//        aManager.set(AlarmManager.RTC_WAKEUP,
//                calendar.getTimeInMillis(), pi);
//    }
//    private void cancel(){
//        Intent intent = new Intent(AlarmTest.this, AlarmActivity.class); intent.setAction("nzy");
//        //这里的action必须和上面设置的action一样 也就是取消的唯一标识
//        PendingIntent pendingIntent = PendingIntent.getActivity( AlarmTest.this, 0, intent, 0);  // 创建PendingIntent对象
//        aManager.cancel(pendingIntent);
//    }

}