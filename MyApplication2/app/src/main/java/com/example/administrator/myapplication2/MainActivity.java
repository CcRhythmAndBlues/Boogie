package com.example.administrator.myapplication2;
import android.Manifest;
import android.app.*;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.util.Random;
/*
* Author cc
* 功能:
*   1.更换按钮：
*       从指定文件夹中更换一张壁纸(后期加钟控定时)
*   2.添加按钮：
*       从图库中选择一张图片加入指定文件夹
*
* 待改进：
*   删除时定位项目目录
*   保存服务是否开启 的变量
*
* */

public class MainActivity extends AppCompatActivity {
    private Button Btn_setWallpaper;
    private Button Btn_add;
    private Button Btn_auto;
    private Button Btn_del;
    private static final int IMAGE_ADD_CODE=666;
    private static final int IMAGE_DEL_CODE=777;
    private String folderpath = null;
    private boolean isAuto = false;
//    public int anHour = 1000*60*60*24;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        folderpath = getExternalFilesDir(null).getAbsolutePath()+"/"+"Boogie";
//        folderpath = "/storage/emulated/0/Android/data/com.example.administrator.myapplication2/files/"+"Wallpaper";
//        folderpath = "/storage/emulated/0/Pictures/"+"Boogie";
        Log.d("日志",folderpath);
        checkNumber();

        //恢复数据
        if (savedInstanceState != null) {
            isAuto = savedInstanceState.getBoolean("isAuto");
        }
        TextView tv_path = findViewById(R.id.tv_path);

        String []p = folderpath.split("/");
        String newpath = "";
        for(int i =4;i<p.length;i++){
            if (p[i].length()>15){
                p[i] = "com.exam...myapplication2";
            }
            newpath = newpath+p[i]+"/";
        }
        tv_path.setText("图片地址:"+newpath);

        Btn_setWallpaper = findViewById(R.id.btn_setWallpaper);//找到按钮
        Btn_add = findViewById(R.id.btn_addtoDrawable);//找到按钮
        Btn_auto = findViewById(R.id.btn_auto);//找到按钮
        Btn_del = findViewById(R.id.btn_del);

        if (!isAuto){
            Btn_auto.setText("未开启");
        }else {
            Btn_auto.setText("已开启");
        }
        setOnClickListener();
        /*
            通过此方法保存当前壁纸的名字
         */
    }

    //预防万一保存的isAuto属性
    @Override
    protected void onSaveInstanceState(Bundle outState) {//保存实例状态，即是保存Activity的数据
        super.onSaveInstanceState(outState);
        boolean isauto = isAuto;
        outState.putBoolean("isAuto", isauto);
    }

    //检查当前库里多少图片壁纸
    private void checkNumber(){
//        String folderpath = Environment.getExternalStorageDirectory().getPath()+"/"+"Pictures/Boogie";
        int imgNum = 0;
        try{
            File file = new File(folderpath);
            if (!file.exists()) {
                file.mkdirs();
            }
            imgNum = file.listFiles().length;
        }catch (Exception e){
            imgNum = 0;
        }
        TextView tv_num=findViewById(R.id.tv_num);
        tv_num.setText(""+imgNum);
    }

    //绑定按钮和触发函数
    private void setOnClickListener(){
        //绑定更换壁纸按钮
//        WallpaperManager mWallManager = WallpaperManager.getInstance(this);
//        ChangeWallpaper changeWallpaper = new ChangeWallpaper();
//        changeWallpaper.mWallManager=mWallManager;
//        Btn_setWallpaper.setOnClickListener(changeWallpaper);
        Btn_setWallpaper.setOnClickListener(new ChangeWallpaper());
        Btn_add.setOnClickListener(new addtoFolderpath());
        Btn_auto.setOnClickListener(new autoChange());
        Btn_del.setOnClickListener(new Delimg());
    }

    //添加到文件夹功能
    private class  addtoFolderpath  implements View.OnClickListener{
        @Override
        public void onClick(View v){
//            Toast.makeText(MainActivity.this, "添加图片功能", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,IMAGE_ADD_CODE);

        }
    }
    //回在addtoDrawable中被调用 按code 666确认
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(resultCode==RESULT_OK&&null!=data){
            Uri uriData=data.getData();
            String[]filePathColum={MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uriData,filePathColum,null,null,null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColum[0]);
            String imageFilePath = cursor.getString(columnIndex);
            cursor.close();
//            Log.d("MainActivity","onActivityResult:"+imageFilePath);
//            Bitmap bitmap=BitmapFactory.decodeFile(imageFilePath);
            if(requestCode==IMAGE_ADD_CODE){
                  copyTodrawable(imageFilePath);
            }else if(requestCode==IMAGE_DEL_CODE){
                //删除
                File file = new File(imageFilePath);
                boolean isDel = file.delete();
                if (isDel){
                    Toast.makeText(MainActivity.this, "图片已删除", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "出错！图片删除失败", Toast.LENGTH_SHORT).show();
                }
                    checkNumber();
            }

        }}

    //根据文件路径 把文件复制一份到drawable
    private void copyTodrawable(String filePath){
        try {
            //这里进行如果文件夹不存在  就创建。
//            String folderpath = Environment.getExternalStorageDirectory().getPath()+"/"+"Pictures/Boogie";
            File folder = new File(folderpath);
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    555);
            if (!folder.exists()){
                        boolean b = folder.mkdirs();
                        String loginfo = b?"创建文件夹成功":"创建文件夹失败";
                        Log.d("日志",loginfo);
            }

            File fromFile=new File(filePath);
            File toFile=new  File(folderpath+"/"+fromFile.getName());

            int i = copyfile(fromFile, toFile, false);
            checkNumber();
            if (i==0){
                Log.d("日志","copyfile成功");
                Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
            }else if(i==2){
                Toast.makeText(MainActivity.this, "添加失败,文件名重复", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "添加失败,请查看日志", Toast.LENGTH_SHORT).show();
            }


        }catch (Exception e){
            Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    //copyTodrawable调用的函数
    public static int copyfile(File fromFile, File toFile,Boolean rewrite )
    {
        if (!fromFile.exists()) {
            Log.d("日志","fromFile不存在！");
            return 1;
        }
        if (!fromFile.isFile()) {
            Log.d("日志","fromFile不是个文件");
            return 1;
        }
        if (!fromFile.canRead()) {
            Log.d("日志","fromFile不可读");
            return 1;
        }
        if(!toFile.getParentFile().exists()||!toFile.exists()){
            boolean b =toFile.mkdirs();
        }
        if (toFile.exists() && !rewrite) {
            Log.d("日志","照片名重复");
            return 2;
//            toFile.delete();
        }
        try {
            java.io.FileInputStream fosfrom = new java.io.FileInputStream(fromFile);
            java.io.FileOutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c); //将内容写到新文件当中
            }
            fosfrom.close();
            fosto.close();
            return 0;
        } catch (Exception ex) {
            Log.d("日志","捕获异常copyfile失败,"+ex);
        }
        return 1;
    }

    //申请权限调用的函数
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 555:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                        String folderpath = Environment.getExternalStorageDirectory().getPath()+"/"+"Pictures/Boogie";
                        File file = new File(folderpath);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                    }
                    break;
                }
        }
    }

    //点击启动自动更换按钮
    private class autoChange implements View.OnClickListener{
        @Override
        public void onClick(View v){
            //xxxx
            File folder = new File(folderpath);
            if (!folder.exists() || folder.listFiles() == null){
                Toast.makeText(MainActivity.this, "文件库不存在或为空,请先添加", Toast.LENGTH_SHORT).show();
                return;
            }
            TextView btn_auto=findViewById(R.id.btn_auto);
            Intent intent=new Intent();
            //与清单文件的receiver的anction对应
            intent.setAction("com.pateo.mybroadcast");
            intent.setComponent(new ComponentName("com.example.administrator.myapplication2",
                    "com.example.administrator.myapplication2.MyAutoBroadUpdateReceiver"));
            Log.d("日志","即将发送广播");

//            Intent i = new Intent(MainActivity.this,MainActivity.MyService.class);
//            i.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
//            intent.putExtra("changeTime",anHour);
            if(!isAuto){
                //如果当前未开启,设为开启
                isAuto=true;
                intent.putExtra("status",isAuto);
                //方案1：发送广播
                sendBroadcast(intent);
                //方案2：不发广播直接开启服务
//                MainActivity.this.startForegroundService(i);


                btn_auto.setText("已开启");
                Toast.makeText(MainActivity.this, "自动更换壁纸已开启", Toast.LENGTH_SHORT).show();
            }else {
                //如果当前已开启，设为false
                isAuto=false;
                intent.putExtra("status",isAuto);
                //方案1：发送广播
                sendBroadcast(intent);
                //方案2：不发广播直接开启服务
//                MainActivity.this.stopService(i);

                btn_auto.setText("未开启");
                Toast.makeText(MainActivity.this, "自动更换壁纸已关闭", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //删除文件夹中壁纸按钮
    private class Delimg implements View.OnClickListener{
        @Override
        public void onClick(View v){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,IMAGE_DEL_CODE);
        }
    }
    //更换壁纸功能
    private class  ChangeWallpaper  implements View.OnClickListener{
        //private WallpaperManager mWallManager = null;
        @Override
        public void onClick(View v){
            File folder = new File(folderpath);
            if (!folder.exists() || folder.listFiles() == null){
                Toast.makeText(MainActivity.this, "文件库不存在或为空,请先添加", Toast.LENGTH_SHORT).show();
                return;
            }
            changeW();
            Toast.makeText(MainActivity.this, "更换成功", Toast.LENGTH_SHORT).show();

        }
    }
    //封装的更换壁纸函数,在自动和手动更换壁纸调用
    public void changeW(){

        try {
            WallpaperManager myWallManager = WallpaperManager.getInstance(MainActivity.this);
            File[] files = new File(folderpath).listFiles();
            int imgnumber = files.length;
            String randomImg = files[new Random().nextInt(imgnumber)].getName();

            Drawable wallpaperDrawable = myWallManager.getDrawable();
//            Log.d("测试日志","wallpaperDrawable:"+wallpaperDrawable.toString());
//            Log.d("测试日志","wallpaperDrawable.getCurrent:"+wallpaperDrawable.getCurrent().toString());

            Bitmap bitmap=BitmapFactory.decodeFile(folderpath+"/"+randomImg);
            myWallManager.setBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}

