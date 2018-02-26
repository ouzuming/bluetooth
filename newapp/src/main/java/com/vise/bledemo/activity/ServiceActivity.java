package com.vise.bledemo.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.vise.bledemo.R;
import com.vise.bledemo.service.MyDemoService;

public class ServiceActivity extends AppCompatActivity implements View.OnClickListener{
    Button btn_serviceStart, btn_serviceStop;
    Button btn_bindService, btn_unbindService, btn_showData;
    private static final String TAG = "TAG_SERVICE";
    private MyDemoService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            downloadBinder = (MyDemoService.DownloadBinder) service;
            downloadBinder.startDownload();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected"+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
            downloadBinder.stopDownload();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        widgetInit();
    }

    private void widgetInit() {
        btn_serviceStart = findViewById(R.id.bt_serviceStart);
        btn_serviceStart.setOnClickListener(this);
        btn_serviceStop = findViewById(R.id.bt_serviceStop);
        btn_serviceStop.setOnClickListener(this);

        btn_bindService = findViewById(R.id.bt_bindService);
        btn_bindService.setOnClickListener(this);
        btn_unbindService = findViewById(R.id.bt_unbindService);
        btn_unbindService.setOnClickListener(this);
        btn_showData = findViewById(R.id.bt_showData);
        btn_showData.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_serviceStart:
                Log.d(TAG, "serviceStart onClick: "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                triggerStartService();
                break;

            case R.id.bt_serviceStop:
                Log.d(TAG, "serviceStop onClick: "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                triggerStopService();
                break;

            case R.id.bt_bindService:
                Log.d(TAG, "bindService onClick: "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                triggerBindService();
                break;

            case R.id.bt_unbindService:
                Log.d(TAG, "unbindService onClick: "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                triggerUnbindService();
                break;

            case R.id.bt_showData:
                Log.d(TAG, "bt_showData onClick: "+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                triggerShowData();
                break;

        }
    }

    private void triggerShowData() {
        int progress = downloadBinder.getProgress();
        Log.d(TAG, "progress: "+ progress+ " [" + Thread.currentThread().getId() + "]" + " [" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
    }

    private void triggerUnbindService() {
        unbindService(connection);
    }

    private void triggerBindService() {
        Intent bindIntern = new Intent(ServiceActivity.this, MyDemoService.class);
        bindService(bindIntern,connection,BIND_AUTO_CREATE);
    }

    private void triggerStopService() {
        Intent intent = new Intent(ServiceActivity.this, MyDemoService.class);
        stopService(intent);
    }

    private void triggerStartService() {
        Intent intent = new Intent(ServiceActivity.this, MyDemoService.class);
        startService(intent);
    }
}
