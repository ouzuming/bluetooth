package com.vise.bledemo.myClass;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by 3ivr on 2018/1/18.
 */

public class MyFileHandle {
   // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    // 判断SD卡是否存在
    public static boolean isSdCardExist(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // 获取SD卡目录路径：
    public static String getSdCardPath(){
        String sdpath = "";
        if(isSdCardExist()){
            sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else {
            return null;
        }
        return sdpath;
    }

    // 获取默认的文件路径
    public static String getDefaultFilePath(){
        String filePath = "";
        File file = new File(Environment.getExternalStorageDirectory(),"abc.bin");
        if(file.exists()){
            filePath = file.getAbsolutePath();
        }else{
            return null;
        }
        return filePath;
    }
    // 获取file input stream
    public static FileInputStream getFileInputStream(){
       // File file = new File(Environment.getExternalStorageDirectory(), "abc.bin");
        //File file = new File("/storage/emulated/0/abc.bin");
        File file = new File("/sdcard/abc.bin");
        if(!file.exists()){
            Log.d("ble_Status", "file is not exist" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            return null;
        }
        FileInputStream inputStream = null;
        try {
            Log.d("ble_Status", "get input stream " + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.d("ble_Status", "error2 "+ e + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            e.printStackTrace();
        }
        Log.d("ble_Status", "return inputStream" + " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        return inputStream;
    }
}
