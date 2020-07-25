package com.example.administrator.myapplication2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class MyAutoBroadUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MyService.class);
        i.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
//        int changeTime = intent.getIntExtra("changeTime",1000*60*60*24);
//        i.putExtra("changeTime",changeTime);

        i.putExtra("isfirst",intent.getBooleanExtra("isfirst",false));

        boolean isAuto = intent.getBooleanExtra("status",true);
        Log.d("日志","广播已收到");
        if(isAuto){
            context.startService(i);
            Log.d("日志","正在启动服务......");
        }else {
            context.stopService(i);
            Log.d("日志","正在关闭服务......");
        }

    }

}
