package com.example.administrator.myapplication2;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import javax.net.ssl.ManagerFactoryParameters;
import java.io.File;
import java.util.Random;



//自动更换壁纸服务
public class MyService extends Service {
    private String folderpath = null;

    private int changeTime =1000*60*60*24;
//    private int changeTime =1000*5;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        folderpath = getExternalFilesDir(null).getPath()+"/"+"Boogie";
//            folderpath = "/storage/emulated/0/Pictures/"+"Boogie";
//            folderpath = "/storage/emulated/0/Android/data/com.example.administrator.myapplication2/files/"+"Wallpaper";

//            Notification notification = setForegroundNotification();
//            startForeground(1,notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        turn = false;
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent2 = new Intent(this, MyAutoBroadUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent2, 0);
        manager.cancel(pi);

        Log.d("日志","已关闭");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        int anHour = intent.getIntExtra("changeTime",1000*60*60*24);
        Log.d("日志","服务已被启动,开始运作");
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + changeTime;
        Intent intent2 = new Intent(this, MyAutoBroadUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent2, 0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "执行了更换操作", Toast.LENGTH_SHORT).show();
    //                这是定时所执行的任务
                        File folder = new File(folderpath);
                        if (!folder.exists() || folder.listFiles() == null){
                            Toast.makeText(getApplicationContext(), "更换壁纸出错", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    try {
                        WallpaperManager myWallManager = WallpaperManager.getInstance(getApplicationContext());
                        File[] files = new File(folderpath).listFiles();
                        int imgnumber = files.length;
                        String randomImg = files[new Random().nextInt(imgnumber)].getName();

                        Bitmap bitmap= BitmapFactory.decodeFile(folderpath+"/"+randomImg);
                        myWallManager.setBitmap(bitmap);
                    } catch (Exception e) {
                        Log.d("日志","捕获异常:"+e);
                        e.printStackTrace();
                    }
                    Looper.loop();
                }
            }).start();
        return super.onStartCommand(intent, flags, startId);
    }
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private Notification setForegroundNotification(){
//        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        String channlld = "access_lock_channel";
//        NotificationChannel channel = new NotificationChannel(channlld,"LockChannel",NotificationManager.IMPORTANCE_DEFAULT);
//
//        nm.createNotificationChannel(channel);
//
////        Log.d("日志","观察数据:MyService中的this:"+this.toString());
//        Notification notification = new Notification.Builder(this).setChannelId(channlld)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle(getString(R.string.app_name))
//                .setAutoCancel(true)
//                .build();
//        return notification;
//    }
}
