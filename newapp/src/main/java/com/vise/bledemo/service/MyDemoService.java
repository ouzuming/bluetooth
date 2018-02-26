package com.vise.bledemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyDemoService extends Service {
    private static final String TAG = "TAG_MY_DEMO_SERVICE";
    private static boolean isWorking = true;
    private static boolean isStartDwonload = false;
    private static int downloadProgress = 0;
    public MyDemoService() {
        Log.d(TAG, "MyDemoService"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
    }

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isWorking = true;
        Log.d(TAG, "onStartCommand"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isWorking){
                    Log.d(TAG, "enter sleep"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");

                    if(isStartDwonload){
                        downloadProgress++; ;
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        isWorking = false;
        Log.d(TAG, "onDestroy"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        super.onDestroy();
    }

   public class DownloadBinder extends Binder{
        public void startDownload(){
            Log.d("DOWNLOAD_BINDER", "startDownload"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            isStartDwonload = true;
        }

        public void stopDownload(){
            Log.d("DOWNLOAD_BINDER", "stopDownload"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            isStartDwonload = false;
        }

        public int getProgress(){
            Log.d("DOWNLOAD_BINDER", "getProgress"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            return downloadProgress;
        }
    }
}


